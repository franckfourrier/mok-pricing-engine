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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplyPricingToTransactionCommandHandler {

    private final ComputeFeeQuery computeFeeQuery;
    private final ComputeTaxQuery computeTaxQuery;
    private final ComputeCommissionDistributionQuery computeCommissionDistributionQuery;
    private final ExternalAccountCreditor externalAccountCreditor;
    private final LedgerWriter ledgerWriter;

    @Value("${ledger.accounts.cantonnement:ACC-CANT}")
    private String accCant;

    @Value("${ledger.accounts.exploitation:ACC-EXP}")
    private String accExp;

    @Value("${ledger.accounts.taxRate:ACC-TAX-RATE}")
    private String accTaxRate;

    @Value("${ledger.accounts.taxFixed:ACC-TAX-FIXED}")
    private String accTaxFixed;

    @Transactional
    public ApplyPricingToTransactionResponse handle(ApplyPricingToTransactionCommand cmd, String actor) {
        validate(cmd);

        PricingRequestContext ctx = new PricingRequestContext(
                cmd.transactionCode(),
                cmd.amount(),
                cmd.accountId(),
                cmd.accountType(),
                cmd.kycValidated(),
                cmd.monthlyTxCount(),
                cmd.occurredAt()
        );

        FeeComputationResult feeRes = computeFeeQuery.computeFee(ctx);
        Money fee = feeRes.fee();

        TaxComputationResult taxRes = computeTaxQuery.computeTax(ctx);
        Money tax = taxRes.tax();

        Money commissionBase = (cmd.transactionCode().transactionType() == TransactionType.DEPOSIT)
                ? estimateWithdrawalFee(ctx)
                : safe(fee);

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

        if (fee != null && !fee.isZero()) {
            postings.add(debit(accCant, fee, LedgerEntryKind.FEE, feeRes.feePolicyId(),
                    "FEE applied tx=" + cmd.externalTxId()));
            postings.add(credit(accExp, fee, LedgerEntryKind.FEE, feeRes.feePolicyId(),
                    "FEE collected tx=" + cmd.externalTxId()));
        }

        if (tax != null && !tax.isZero()) {
            String debitAccount = (taxRes.taxMode() == TaxMode.EXPLOITATION) ? accExp : accCant;
            String creditAccount = (taxRes.strategyType() == TaxStrategyType.FIXED_AMOUNT) ? accTaxFixed : accTaxRate;
            LedgerEntryKind kind = (taxRes.strategyType() == TaxStrategyType.FIXED_AMOUNT)
                    ? LedgerEntryKind.TAX_FIXED
                    : LedgerEntryKind.TAX_RATE;

            postings.add(debit(debitAccount, tax, kind, taxRes.taxPolicyId(),
                    "TAX debit (" + taxRes.taxMode() + ") tx=" + cmd.externalTxId()));
            postings.add(credit(creditAccount, tax, kind, taxRes.taxPolicyId(),
                    "TAX credited tx=" + cmd.externalTxId()));
        }

        Money expDebit = (cmd.transactionCode().transactionType() == TransactionType.DEPOSIT)
                ? externalTotal
                : agentExternal;

        if (expDebit != null && !expDebit.isZero()) {
            postings.add(debit(accExp, expDebit, LedgerEntryKind.COMMISSION, comRes.commissionPlanId(),
                    "COMMISSION payout tx=" + cmd.externalTxId()));
        }

        Money kratos = sumForBeneficiary(comRes, "KRATOS");
        if (kratos != null && !kratos.isZero()) {
            postings.add(debit(accCant, kratos, LedgerEntryKind.COMMISSION, comRes.commissionPlanId(),
                    "COMMISSION KRATOS tx=" + cmd.externalTxId()));
            postings.add(credit(accExp, kratos, LedgerEntryKind.COMMISSION, comRes.commissionPlanId(),
                    "COMMISSION KRATOS revenue tx=" + cmd.externalTxId()));
        }

        RecordLedgerTransactionResponse ledgerRes = ledgerWriter.record(
                new RecordLedgerTransactionCommand(cmd.externalTxId(), cmd.occurredAt(), postings),
                actor
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

    private Money estimateWithdrawalFee(PricingRequestContext ctx) {
        PricingRequestContext wCtx = new PricingRequestContext(
                resolveWithdrawalTransactionCode(ctx),
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

    private TransactionCode resolveWithdrawalTransactionCode(PricingRequestContext ctx) {
        return switch (ctx.accountType()) {
            case AGENT, DISTRIBUTOR -> TransactionCode.AGENT_DISTRIBUTOR_WITHDRAWAL;
            case STANDARD, PREMIUM, SUBSCRIBER -> TransactionCode.SUBSCRIBER_WITHDRAWAL;
            default -> throw new DomainValidationException(
                    "WITHDRAWAL_TRANSACTION_CODE_NOT_RESOLVED",
                    "Cannot resolve withdrawal transaction code for account type",
                    Map.of("accountType", ctx.accountType().name())
            );
        };
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