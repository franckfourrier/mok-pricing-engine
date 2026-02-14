package com.kratos.mok.pricing.app.application.command.applyPricingToTransaction;

import com.kratos.mok.pricing.fees.application.port.ComputeFeeQuery;
import com.kratos.mok.pricing.fees.application.port.FeeComputationResult;
import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionCommand;
import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionResponse;
import com.kratos.mok.pricing.ledger.application.port.LedgerWriter;
import com.kratos.mok.pricing.ledger.domain.Posting;
import com.kratos.mok.pricing.ledger.domain.enums.EntryDirection;
import com.kratos.mok.pricing.ledger.domain.enums.LedgerEntryKind;
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
                cmd.type(),
                cmd.amount(),
                cmd.payerAccountId(),
                cmd.payerAccountType(),
                cmd.kycValidated(),
                cmd.monthlyTxCount(),
                cmd.occurredAt()
        );

        // 1) Fee
        FeeComputationResult feeRes = computeFeeQuery.computeFee(ctx);
        Money fee = feeRes.fee();

        // 2) Tax
        TaxComputationResult taxRes = computeTaxQuery.computeTax(ctx);
        Money tax = taxRes.tax();

        // 3) Ledger postings (internal accounts only)
        List<Posting> postings = new ArrayList<>();

        // ---- Fees: cantonnement (DEBIT) -> exploitation (CREDIT)
        if (fee != null && !fee.isZero()) {
            postings.add(debit(accCant, fee, LedgerEntryKind.FEE, feeRes.feePolicyId(),
                    "FEE applied for tx=" + cmd.externalTxId()));
            postings.add(credit(accExp, fee, LedgerEntryKind.FEE, feeRes.feePolicyId(),
                    "FEE collected for tx=" + cmd.externalTxId()));
        }

        // ---- Taxes:
        // debit from (cantonnement|exploitation) depending on mode
        // credit to (taxRate|taxFixed) depending on strategy
        if (tax != null && !tax.isZero()) {
            TaxMode taxMode = taxRes.taxMode();
            TaxStrategyType strategyType = taxRes.strategyType();

            String debitAccount = (taxMode == TaxMode.EXPLOITATION) ? accExp : accCant;
            String creditAccount = (strategyType == TaxStrategyType.FIXED_AMOUNT) ? accTaxFixed : accTaxRate;

            LedgerEntryKind kind = (strategyType == TaxStrategyType.FIXED_AMOUNT)
                    ? LedgerEntryKind.TAX_FIXED
                    : LedgerEntryKind.TAX_RATE;

            postings.add(debit(debitAccount, tax, kind, taxRes.taxPolicyId(),
                    "TAX debit (" + taxMode + ") for tx=" + cmd.externalTxId()));
            postings.add(credit(creditAccount, tax, kind, taxRes.taxPolicyId(),
                    "TAX credited to sub-account for tx=" + cmd.externalTxId()));
        }

        // 4) record ledger transaction (idempotent by externalTxId côté ledger)
        RecordLedgerTransactionCommand ledgerCmd =
                new RecordLedgerTransactionCommand(cmd.externalTxId(), cmd.occurredAt(), postings);

        RecordLedgerTransactionResponse ledgerRes = ledgerWriter.record(ledgerCmd, actor);

        return new ApplyPricingToTransactionResponse(
                cmd.externalTxId(),
                feeRes.feePolicyId(),
                fee,
                taxRes.taxPolicyId(),
                tax,
                ledgerRes.externalTxId(),
                ledgerRes.recorded()
        );
    }

    // -------------------------
    // Posting helpers
    // -------------------------
    private Posting debit(String accountCode, Money m, LedgerEntryKind kind, String policyId, String desc) {
        return new Posting(accountCode, EntryDirection.DEBIT, m.amount().toPlainString(), m.currency(), kind, policyId, desc);
    }

    private Posting credit(String accountCode, Money m, LedgerEntryKind kind, String policyId, String desc) {
        return new Posting(accountCode, EntryDirection.CREDIT, m.amount().toPlainString(), m.currency(), kind, policyId, desc);
    }

    private void validate(ApplyPricingToTransactionCommand cmd) {
        if (cmd == null) throw new IllegalArgumentException("command is required");
        if (cmd.externalTxId() == null || cmd.externalTxId().isBlank()) {
            throw new DomainValidationException("EXTERNAL_TX_ID_REQUIRED", "externalTxId is required", Map.of());
        }
        if (cmd.type() == null) {
            throw new DomainValidationException("TRANSACTION_TYPE_REQUIRED", "transaction type is required", Map.of());
        }
        if (cmd.amount() == null) {
            throw new DomainValidationException("AMOUNT_REQUIRED", "amount is required", Map.of());
        }
        if (cmd.amount().isNegative() || cmd.amount().isZero()) {
            throw new DomainValidationException("INVALID_AMOUNT", "amount must be > 0",
                    Map.of("amount", cmd.amount().toString()));
        }
        if (cmd.currency() == null || cmd.currency().isBlank()) {
            throw new DomainValidationException("CURRENCY_REQUIRED", "currency is required", Map.of());
        }
        if (cmd.payerAccountType() == null) {
            throw new DomainValidationException("PAYER_ACCOUNT_TYPE_REQUIRED", "payerAccountType is required", Map.of());
        }
        if (cmd.payerAccountId() == null || cmd.payerAccountId().isBlank()) {
            throw new DomainValidationException("PAYER_ACCOUNT_ID_REQUIRED", "payerAccountId is required", Map.of());
        }
        if (cmd.occurredAt() == null) {
            throw new DomainValidationException("OCCURRED_AT_REQUIRED", "occurredAt is required", Map.of());
        }
    }
}
