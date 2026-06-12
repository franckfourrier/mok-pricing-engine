package com.kratos.mok.pricing.app.application.command.applyPricingToTransaction;

import com.kratos.mok.pricing.app.api.ExternalAccountCreditor;
import com.kratos.mok.pricing.commissions.application.port.ComputeCommissionDistributionQuery;
import com.kratos.mok.pricing.commissions.application.port.ComputeCommissionDistributionQuery.CommissionDistributionResult;
import com.kratos.mok.pricing.fees.application.port.ComputeFeeQuery;
import com.kratos.mok.pricing.fees.application.port.FeeComputationResult;
import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionCommand;
import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionResponse;
import com.kratos.mok.pricing.ledger.application.port.LedgerWriter;
import com.kratos.mok.pricing.ledger.domain.Posting;
import com.kratos.mok.pricing.ledger.domain.enums.EntryDirection;
import com.kratos.mok.pricing.ledger.domain.enums.LedgerEntryKind;
import com.kratos.mok.pricing.ledger.domain.event.LedgerEntriesCreatedEvent;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import com.kratos.mok.pricing.taxes.application.port.ComputeTaxQuery;
import com.kratos.mok.pricing.taxes.application.port.TaxComputationResult;
import com.kratos.mok.pricing.taxes.application.port.TaxLine;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.kratos.mok.pricing.shared.domain.enums.TransactionCode.*;

@Service
@RequiredArgsConstructor
public class ApplyPricingToTransactionCommandHandler {

  private final ComputeFeeQuery computeFeeQuery;
  private final ComputeTaxQuery computeTaxQuery;
  private final ComputeCommissionDistributionQuery computeCommissionDistributionQuery;
  private final ExternalAccountCreditor externalAccountCreditor;
  private final LedgerWriter ledgerWriter;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${ledger.accounts.cantonment:ACC-CANT}")
  private String accCant;

  @Value("${ledger.accounts.exploitation}")
  private String accExp;

  @Value("${ledger.accounts.tax}")
  private String accTax;

  @Value("${ledger.accounts.taxRate}")
  private String accTaxRate;

  @Value("${ledger.accounts.taxFixed}")
  private String accTaxFixed;

  @Value("${ledger.accounts.distributed}")
  private String accDist;

  @Value("${ledger.accounts.distributedSuperDistributor}")
  private String accDistSuperDistributor;

  @Value("${ledger.accounts.distributedDistributor}")
  private String accDistDistributor;

  @Value("${ledger.accounts.distributedAgent}")
  private String accDistAgent;

  @Value("${ledger.accounts.external}")
  private String accExt;

  @Transactional
  public ApplyPricingToTransactionResponse handle(ApplyPricingToTransactionCommand cmd, String actor) {
    validate(cmd);

    Map<String, String> hierarchy = Map.of(
        "AGENT", cmd.accountId(),
        "DISTRIBUTOR", cmd.distributorAccountId() != null ? cmd.distributorAccountId() : "",
        "SUPER_DISTRIBUTOR", cmd.superDistributorAccountId() != null ? cmd.superDistributorAccountId() : ""
    );

    PricingRequestContext ctx = new PricingRequestContext(
        cmd.transactionCode(),
        cmd.amount(),
        cmd.accountId(),
        cmd.accountType(),
        cmd.kycValidated(),
        cmd.monthlyTxCount(),
        cmd.occurredAt(),
        hierarchy
    );

    // 1. Calcul des Frais
    FeeComputationResult feeRes = ctx.transactionCode().supportsFees()
        ? computeFeeQuery.computeFee(ctx)
        : new FeeComputationResult("NONE", Money.ZERO);

    Money fee = safe(feeRes.fee());

    // 2. Calcul des Taxes
    TaxComputationResult taxRes = ctx.transactionCode().supportsTaxes()
        ? computeTaxQuery.computeTax(ctx)
        : new TaxComputationResult(Money.ZERO, List.of());

    Money tax = safe(taxRes.totalTax());

    // 3. Calcul des Commissions
    Money commissionBase = switch (cmd.transactionCode()) {
      case SUBSCRIBER_WITHDRAWAL, SUBSCRIBER_DEPOSIT -> estimateSubscriberWithdrawalFee(ctx);
      case SUBSCRIBER_P2P_TRANSFER -> estimateSubscriberP2PTranferFee(ctx);
      case MERCHANT_SETTLEMENT_SUBSCRIBER -> estimateMerchantSettlementSubscriberFee(ctx);
      case MERCHANT_SETTLEMENT_EXTERNAL -> estimateMerchantSettlementExternalFee(ctx);
      case SUBSCRIBER_EXTERNAL_P2P_TRANSFER -> estimateSubscriberExternalP2PTranferFee(ctx);
      default -> safe(fee);
    };

    CommissionDistributionResult comRes = computeCommissionDistributionQuery.compute(ctx, commissionBase);

    // === FILTRAGE IMPORTANT : On retire TAX_RATE des payouts ===
    List<CommissionDistributionResult.Line> commissionLines = comRes.lines().stream()
        .filter(line -> line.beneficiary() != null
            && !"TAX_RATE".equalsIgnoreCase(line.beneficiary())
            && !"KRATOS".equalsIgnoreCase(line.beneficiary()))
        .toList();

    // Calcul des totaux pour commissions
    Money externalTotal = Money.ZERO;
    Money agentExternal = Money.ZERO;

    for (var line : commissionLines) {
      String beneficiary = line.beneficiary().trim().toUpperCase();
      Money amt = line.amount();
      if (amt == null || amt.isZero()) continue;

      String accountId = resolveBeneficiaryAccountId(beneficiary, cmd);
      if (accountId == null || accountId.isBlank()) {
        throw new DomainValidationException(
            "BENEFICIARY_ACCOUNT_MISSING",
            "Missing accountId for beneficiary=" + beneficiary,
            Map.of("beneficiary", beneficiary, "externalTxId", cmd.externalTxId())
        );
      }

      externalTotal = externalTotal.add(amt);
      if ("AGENT".equals(beneficiary)) {
        agentExternal = agentExternal.add(amt);
      }
    }

    // ====================== ÉCRITURES COMPTABLES ======================
    List<Posting> postings = new ArrayList<>();

    // Frais
    if (fee != null && !fee.isZero()) {
      postings.add(debit(accCant, fee, LedgerEntryKind.FEE, feeRes.feePolicyId(),
          "FEE applied tx=" + cmd.externalTxId()));
      postings.add(credit(accExp, fee, LedgerEntryKind.FEE, feeRes.feePolicyId(),
          "FEE collected tx=" + cmd.externalTxId()));
    }

    // Taxes (via taxRes.lines())
    for (TaxLine taxLine : taxRes.lines()) {
      Money amount = taxLine.amount();
      if (amount == null || amount.isZero()) continue;

      String sourceAccount = (taxLine.taxMode() == TaxMode.EXPLOITATION) ? accExp : accCant;
      String subTaxAccount = (taxLine.strategyType() == TaxStrategyType.FIXED_AMOUNT) ? accTaxFixed : accTaxRate;
      LedgerEntryKind kind = (taxLine.strategyType() == TaxStrategyType.FIXED_AMOUNT) ? LedgerEntryKind.TAX_FIXED : LedgerEntryKind.TAX_RATE;

      postings.add(debit(sourceAccount, amount, kind, taxLine.taxPolicyId(),
          "TAX debit (" + taxLine.taxMode() + ") tx=" + cmd.externalTxId()));
      postings.add(credit(accTax, amount, kind, taxLine.taxPolicyId(),
          "TAX credited tx=" + cmd.externalTxId()));
      postings.add(credit(subTaxAccount, amount, kind, taxLine.taxPolicyId(),
          "TAX credited tx=" + cmd.externalTxId()));
    }

    // Commissions / Payouts (uniquement les bénéficiaires business)
    Money expDebit = switch (cmd.transactionCode()) {
      case SUBSCRIBER_DEPOSIT -> externalTotal;
      case SUBSCRIBER_WITHDRAWAL -> agentExternal;
      default -> Money.ZERO;
    };

    if (expDebit != null && !expDebit.isZero()) {
      postings.add(debit(accExp, expDebit, LedgerEntryKind.COMMISSION, comRes.commissionPlanId(),
          "COMMISSION payout total tx=" + cmd.externalTxId()));
      postings.add(credit(accDist, expDebit, LedgerEntryKind.COMMISSION, comRes.commissionPlanId(),
          "COMMISSION payout total tx=" + cmd.externalTxId()));

      for (var line : commissionLines) {
        if (line.amount() == null || line.amount().isZero()) continue;

        String beneficiary = line.beneficiary().toUpperCase();
        String targetSubAccount = switch (beneficiary) {
          case "SUPER_DISTRIBUTOR" -> accDistSuperDistributor;
          case "DISTRIBUTOR" -> accDistDistributor;
          case "AGENT" -> accDistAgent;
          default -> null;
        };

        if (targetSubAccount != null) {
          postings.add(credit(targetSubAccount, line.amount(), LedgerEntryKind.COMMISSION, comRes.commissionPlanId(),
              "COMMISSION payout " + beneficiary + " tx=" + cmd.externalTxId()));
        }
      }
    }
    // Enregistrement dans le ledger
    RecordLedgerTransactionResponse ledgerRes = ledgerWriter.record(
        new RecordLedgerTransactionCommand(cmd.externalTxId(), cmd.occurredAt(), postings), actor);

    eventPublisher.publishEvent(new LedgerEntriesCreatedEvent(cmd.externalTxId(), cmd.occurredAt()));

    return ApplyPricingToTransactionResponse.fromDomain(
        cmd.externalTxId(),
        fee,
        tax,
        commissionLines,
        externalTotal,
        ledgerRes.recorded()
    );
  }

  private Money estimateSubscriberWithdrawalFee(PricingRequestContext ctx) {
    PricingRequestContext wCtx = new PricingRequestContext(
        TransactionCode.SUBSCRIBER_WITHDRAWAL,
        ctx.amount(),
        ctx.accountId(),
        ctx.accountType(),
        ctx.kycValidated(),
        ctx.monthlyTxCount(),
        ctx.occurredAt(),
        ctx.hierarchy()
    );
    FeeComputationResult res = computeFeeQuery.computeFee(wCtx);
    return safe(res.fee());
  }

  private Money estimateSubscriberP2PTranferFee(PricingRequestContext ctx) {
    PricingRequestContext wCtx = new PricingRequestContext(
        SUBSCRIBER_P2P_TRANSFER,
        ctx.amount(),
        ctx.accountId(),
        ctx.accountType(),
        ctx.kycValidated(),
        ctx.monthlyTxCount(),
        ctx.occurredAt()
    );
    FeeComputationResult res = computeFeeQuery.computeFee(wCtx);
    return safe(res.fee());
  }

  private Money estimateMerchantSettlementSubscriberFee(PricingRequestContext ctx) {
    PricingRequestContext wCtx = new PricingRequestContext(
        MERCHANT_SETTLEMENT_SUBSCRIBER,
        ctx.amount(),
        ctx.accountId(),
        ctx.accountType(),
        ctx.kycValidated(),
        ctx.monthlyTxCount(),
        ctx.occurredAt()
    );
    FeeComputationResult res = computeFeeQuery.computeFee(wCtx);
    return safe(res.fee());
  }

  private Money estimateMerchantSettlementExternalFee(PricingRequestContext ctx) {
    PricingRequestContext wCtx = new PricingRequestContext(
        MERCHANT_SETTLEMENT_EXTERNAL,
        ctx.amount(),
        ctx.accountId(),
        ctx.accountType(),
        ctx.kycValidated(),
        ctx.monthlyTxCount(),
        ctx.occurredAt()
    );
    FeeComputationResult res = computeFeeQuery.computeFee(wCtx);
    return safe(res.fee());
  }

  private Money estimateSubscriberExternalP2PTranferFee(PricingRequestContext ctx) {
    PricingRequestContext wCtx = new PricingRequestContext(
        SUBSCRIBER_EXTERNAL_P2P_TRANSFER,
        ctx.amount(),
        ctx.accountId(),
        ctx.accountType(),
        ctx.kycValidated(),
        ctx.monthlyTxCount(),
        ctx.occurredAt()
    );
    FeeComputationResult res = computeFeeQuery.computeFee(wCtx);
    return safe(res.fee());
  }

  private Money safe(Money m) {
    return (m == null) ? Money.ZERO : m;
  }

  private String resolveBeneficiaryAccountId(String beneficiary, ApplyPricingToTransactionCommand cmd) {
    return switch (beneficiary.toUpperCase()) {
      case "AGENT" -> cmd.accountId();
      case "DISTRIBUTOR" -> cmd.distributorAccountId();
      case "SUPER_DISTRIBUTOR" -> cmd.superDistributorAccountId();
      case "TAX_RATE", "KRATOS" -> null;
      default -> null;
    };
  }


  private Posting debit(String accountCode, Money m, LedgerEntryKind kind, String policyId, String desc) {
    return new Posting(accountCode, EntryDirection.DEBIT, m.amount().toPlainString(), m.currency(), kind, policyId, desc);
  }

  private Posting credit(String accountCode, Money m, LedgerEntryKind kind, String policyId, String desc) {
    return new Posting(accountCode, EntryDirection.CREDIT, m.amount().toPlainString(), m.currency(), kind, policyId, desc);
  }

  private void validate(ApplyPricingToTransactionCommand cmd) {
    if (cmd == null) {
      throw new IllegalArgumentException("command is required");
    }
    if (blank(cmd.externalTxId())) {
      throw new DomainValidationException("EXTERNAL_TX_ID_REQUIRED", "externalTxId is required", Map.of());
    }
    if (cmd.transactionCode() == null) {
      throw new DomainValidationException("TRANSACTION_CODE_REQUIRED", "transactionCode is required", Map.of());
    }
    if (cmd.amount() == null || cmd.amount().isZero() || cmd.amount().isNegative()) {
      throw new DomainValidationException("INVALID_AMOUNT", "amount must be > 0",
          Map.of("amount", String.valueOf(cmd.amount())));
    }
    if (blank(cmd.currency())) {
      throw new DomainValidationException("CURRENCY_REQUIRED", "currency is required", Map.of());
    }
    if (blank(cmd.accountId())) {
      throw new DomainValidationException("AGENT_ACCOUNT_REQUIRED", "agentAccountId is required", Map.of());
    }
    if (cmd.accountType() == null) {
      throw new DomainValidationException("AGENT_ACCOUNT_TYPE_REQUIRED", "agentAccountType is required", Map.of());
    }
    if (cmd.occurredAt() == null) {
      throw new DomainValidationException("OCCURRED_AT_REQUIRED", "occurredAt is required", Map.of());
    }

    if (cmd.transactionCode().transactionType() == TransactionType.DEPOSIT) {
      if (blank(cmd.distributorAccountId())) {
        throw new DomainValidationException("DISTRIBUTOR_REQUIRED", "distributorAccountId required for DEPOSIT", Map.of());
      }
      if (blank(cmd.superDistributorAccountId())) {
        throw new DomainValidationException("SUPER_DISTRIBUTOR_REQUIRED", "superDistributorAccountId required for DEPOSIT", Map.of());
      }
    }
  }

  private boolean blank(String s) {
    return s == null || s.isBlank();
  }
}
