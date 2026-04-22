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
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.kratos.mok.pricing.shared.domain.enums.TransactionCode.SUBSCRIBER_DEPOSIT;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @Value("${ledger.accounts.external}")
    private String accExt;

    @Transactional
    public ApplyPricingToTransactionResponse handle(ApplyPricingToTransactionCommand cmd, String actor) {
        validate(cmd);

        // On crée la map de hiérarchie
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

        // 1. Calcul des Frais (uniquement si supportés)
        FeeComputationResult feeRes;
        if (ctx.transactionCode().supportsFees()) {
            feeRes = computeFeeQuery.computeFee(ctx);
        } else {
            feeRes = new FeeComputationResult("NONE", Money.ZERO);
        }
        Money fee = safe(feeRes.fee());

        // 2. Calcul des Taxes (uniquement si supportées)
        TaxComputationResult taxRes;
        if (ctx.transactionCode().supportsTaxes()) {
            taxRes = computeTaxQuery.computeTax(ctx);
        } else {
            taxRes = new TaxComputationResult(
                    "NONE",
                    Money.ZERO,
                    TaxMode.NONE,
                    TaxStrategyType.NONE
            );
        }
        Money tax = safe(taxRes.tax());

        Money commissionBase = switch (cmd.transactionCode()) {
            case SUBSCRIBER_WITHDRAWAL -> estimateSubscriberWithdrawalFee(ctx);
            case SUBSCRIBER_DEPOSIT    -> estimateSubscriberWithdrawalFee(ctx);
            case SUBSCRIBER_P2P_TRANSFER    -> estimateSubscriberP2PTranferFee(ctx);
            case SUBSCRIBER_EXTERNAL_P2P_TRANSFER    -> estimateSubscriberExternalP2PTranferFee(ctx);
            default                    -> safe(fee);
        };

        CommissionDistributionResult comRes = computeCommissionDistributionQuery.compute(ctx, commissionBase);

        Money externalTotal = Money.ZERO;
        Money agentExternal = Money.ZERO;

        for (var line : comRes.lines()) {
            String b = line.beneficiary() == null ? "" : line.beneficiary().trim().toUpperCase();
            Money amt = line.amount();

            if (amt == null || amt.isZero()) continue;

            if ("KRATOS".equals(b)) {
                continue;
            }

            String accountId = resolveBeneficiaryAccountId(b, cmd);
            if (accountId == null || accountId.isBlank()) {
                throw new DomainValidationException(
                        "BENEFICIARY_ACCOUNT_MISSING",
                        "Missing accountId for beneficiary=" + b,
                        Map.of("beneficiary", b, "externalTxId", cmd.externalTxId())
                );
            }

            /*String idempotencyKey = cmd.externalTxId() + ":COM:" + b;

            externalAccountCreditor.credit(
                    b,
                    accountId,
                    amt,
                    idempotencyKey,
                    "Commission " + b + " for tx=" + cmd.externalTxId()
            );*/

            externalTotal = externalTotal.add(amt);
            if ("AGENT".equals(b)) {
                agentExternal = agentExternal.add(amt);
            }
        }

        List<Posting> postings = new ArrayList<>();

        /* Ecritures comptables pour les frais */
        if (fee != null && !fee.isZero()) {
            postings.add(debit(accCant, fee, LedgerEntryKind.FEE, feeRes.feePolicyId(),
                    "FEE applied tx=" + cmd.externalTxId()));
            postings.add(credit(accExp, fee, LedgerEntryKind.FEE, feeRes.feePolicyId(),
                    "FEE collected tx=" + cmd.externalTxId()));
        }

        /* Ecritures comptables pour les taxes */
        if (tax != null && !tax.isZero()) {
            String cantonmentOrExploitationAccount = (taxRes.taxMode() == TaxMode.EXPLOITATION) ? accExp : accCant;
            String taxAccount = accTax;
            String subTaxAccount = (taxRes.strategyType() == TaxStrategyType.FIXED_AMOUNT) ? accTaxFixed : accTaxRate;
            LedgerEntryKind kind = (taxRes.strategyType() == TaxStrategyType.FIXED_AMOUNT)
                    ? LedgerEntryKind.TAX_FIXED
                    : LedgerEntryKind.TAX_RATE;

            postings.add(debit(cantonmentOrExploitationAccount, tax, kind, taxRes.taxPolicyId(),
                    "TAX debit (" + taxRes.taxMode() + ") tx=" + cmd.externalTxId()));
            postings.add(credit(taxAccount, tax, kind, taxRes.taxPolicyId(),
                    "TAX credited tx=" + cmd.externalTxId()));
            postings.add(credit(subTaxAccount, tax, kind, taxRes.taxPolicyId(),
                    "TAX credited tx=" + cmd.externalTxId()));
        }

        Money expDebit = switch (cmd.transactionCode()) {
            case SUBSCRIBER_DEPOSIT    -> externalTotal;
            case SUBSCRIBER_WITHDRAWAL -> agentExternal;
            case SUBSCRIBER_P2P_TRANSFER -> Money.ZERO;
            case SUBSCRIBER_EXTERNAL_P2P_TRANSFER -> Money.ZERO;
            default                    -> Money.ZERO;
        };

        /* Prise en compte du paiement de commissions*/
        if (expDebit != null && !expDebit.isZero()) {
            postings.add(debit(accExp, expDebit, LedgerEntryKind.COMMISSION, comRes.commissionPlanId(),
                    "COMMISSION payout tx=" + cmd.externalTxId()));
            postings.add(credit(accDist, expDebit, LedgerEntryKind.COMMISSION, comRes.commissionPlanId(),
                    "COMMISSION payout tx=" + cmd.externalTxId()));
        }

        /*Money kratos = sumForBeneficiary(comRes, "KRATOS");
        if (kratos != null && !kratos.isZero()) {
            postings.add(debit(accCant, kratos, LedgerEntryKind.COMMISSION, comRes.commissionPlanId(),
                    "COMMISSION KRATOS tx=" + cmd.externalTxId()));
            postings.add(credit(accExp, kratos, LedgerEntryKind.COMMISSION, comRes.commissionPlanId(),
                    "COMMISSION KRATOS revenue tx=" + cmd.externalTxId()));
        }*/

        RecordLedgerTransactionResponse ledgerRes = ledgerWriter.record(
                new RecordLedgerTransactionCommand(cmd.externalTxId(), cmd.occurredAt(), postings),
                actor
        );

        eventPublisher.publishEvent(
                new LedgerEntriesCreatedEvent(
                        cmd.externalTxId(),
                        cmd.occurredAt()
                )
        );

        return ApplyPricingToTransactionResponse.fromDomain(
                cmd.externalTxId(),
                fee,
                tax,
                comRes.lines(),
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
                TransactionCode.SUBSCRIBER_P2P_TRANSFER,
                ctx.amount(),
                ctx.accountId(),
                ctx.accountType(),
                ctx.kycValidated(),
                ctx.monthlyTxCount(),
                ctx.occurredAt()
        );
        FeeComputationResult res = computeFeeQuery.computeFee(ctx);
        return safe(res.fee());
    }

    private Money estimateSubscriberExternalP2PTranferFee(PricingRequestContext ctx) {
        PricingRequestContext wCtx = new PricingRequestContext(
                TransactionCode.SUBSCRIBER_EXTERNAL_P2P_TRANSFER,
                ctx.amount(),
                ctx.accountId(),
                ctx.accountType(),
                ctx.kycValidated(),
                ctx.monthlyTxCount(),
                ctx.occurredAt()
        );
        FeeComputationResult res = computeFeeQuery.computeFee(ctx);
        return safe(res.fee());
    }

    private Money safe(Money m) {
        return (m == null) ? Money.ZERO : m;
    }

    private String resolveBeneficiaryAccountId(String beneficiary, ApplyPricingToTransactionCommand cmd) {
        return switch (beneficiary) {
            case "AGENT" -> cmd.accountId();
            case "DISTRIBUTOR" -> cmd.distributorAccountId();
            case "SUPER_DISTRIBUTOR" -> cmd.superDistributorAccountId();
            default -> null;
        };
    }

    private Money sumForBeneficiary(CommissionDistributionResult res, String beneficiary) {
        Money sum = Money.ZERO;
        for (var l : res.lines()) {
            if (l == null || l.amount() == null) continue;
            if (beneficiary.equalsIgnoreCase(l.beneficiary())) {
                sum = sum.add(l.amount());
            }
        }
        return sum;
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