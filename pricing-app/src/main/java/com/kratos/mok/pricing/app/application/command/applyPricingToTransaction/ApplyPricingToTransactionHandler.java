package com.kratos.mok.pricing.app.application.command.applyPricingToTransaction;


import com.kratos.mok.pricing.fees.application.port.ComputeFeeQuery;
import com.kratos.mok.pricing.fees.application.port.FeeComputationResult;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplyPricingToTransactionHandler {

    private final ComputeFeeQuery computeFeeQuery;
    private final ComputeTaxQuery computeTaxQuery;

    private final RecordLedgerTransactionHandler ledgerHandler;

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

        // 1) Contexte commun de pricing (tu l'as déjà dans shared)
        PricingRequestContext ctx = PricingRequestContext.builder()
                .externalTransactionId(cmd.externalTxId())
                .transactionType(cmd.type())
                .amount(cmd.amount())
                .currency(cmd.currency())
                .payerAccountId(cmd.payerAccountId())
                .payerAccountType(cmd.payerAccountType())
                .occurredAt(cmd.occurredAt())
                .build();

        // 2) Compute fee
        FeeComputationResult feeRes = computeFeeQuery.computeFee(ctx);
        Money fee = feeRes.fee();

        // 3) Compute tax
        TaxComputationResult taxRes = computeTaxQuery.computeTax(ctx);
        Money tax = taxRes.tax();

        // 4) Build ledger postings (NO debit user account; ONLY internal accounts)
        List<RecordLedgerTransactionCommand.Posting> postings = new ArrayList<>();

        // ---- Fees: debit cantonnement, credit exploitation
        if (fee != null && !fee.isZero()) {
            postings.add(RecordLedgerTransactionCommand.Posting.debit(accCant, fee));
            postings.add(RecordLedgerTransactionCommand.Posting.credit(accExp, fee));
        }

        // ---- Taxes: debit (cantonnement|exploitation) depending on mode, credit to tax sub-account depending on strategy
        if (tax != null && !tax.isZero()) {
            TaxMode taxMode = taxRes.taxMode();                 // <-- à exposer dans TaxComputationResult
            TaxStrategyType taxStrategy = taxRes.strategyType(); // <-- à exposer dans TaxComputationResult

            String debitAccount = (taxMode == TaxMode.EXPLOITATION) ? accExp : accCant;
            String creditAccount = (taxStrategy == TaxStrategyType.FIXED_AMOUNT) ? accTaxFixed : accTaxRate;

            postings.add(RecordLedgerTransactionCommand.Posting.debit(debitAccount, tax));
            postings.add(RecordLedgerTransactionCommand.Posting.credit(creditAccount, tax));
        }

        // 5) Persist in ledger (idempotent by externalTxId)
        RecordLedgerTransactionCommand ledgerCmd = new RecordLedgerTransactionCommand(
                cmd.externalTxId(),
                cmd.type(),
                cmd.occurredAt(),
                actor,
                postings,
                Map.of(
                        "feePolicyId", feeRes.feePolicyId(),
                        "taxPolicyId", taxRes.taxPolicyId()
                )
        );

        RecordLedgerTransactionResponse ledgerRes = ledgerHandler.handle(ledgerCmd);

        return new ApplyPricingToTransactionResponse(
                cmd.externalTxId(),
                feeRes.feePolicyId(),
                fee,
                taxRes.taxPolicyId(),
                tax,
                ledgerRes.ledgerTransactionId(),
                ledgerRes.recorded()
        );
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
            throw new DomainValidationException("INVALID_AMOUNT", "amount must be > 0", Map.of("amount", cmd.amount().toString()));
        }
        if (cmd.currency() == null || cmd.currency().isBlank()) {
            throw new DomainValidationException("CURRENCY_REQUIRED", "currency is required", Map.of());
        }
        if (cmd.payerAccountType() == null || cmd.payerAccountType().isBlank()) {
            throw new DomainValidationException("PAYER_ACCOUNT_TYPE_REQUIRED", "payerAccountType is required", Map.of());
        }
        if (cmd.payerAccountId() == null || cmd.payerAccountId().isBlank()) {
            throw new DomainValidationException("PAYER_ACCOUNT_ID_REQUIRED", "payerAccountId is required", Map.of());
        }
        if (cmd.occurredAt() == null) {
            throw new DomainValidationException("OCCURRED_AT_REQUIRED", "occurredAt is required", Map.of());
        }
    }

    // -------------------------
    // DTOs (command/response)
    // -------------------------

    public record ApplyPricingToTransactionCommand(
            String externalTxId,
            TransactionType type,
            Money amount,
            String currency,
            String payerAccountId,
            String payerAccountType,
            LocalDateTime occurredAt
    ) {}

    public record ApplyPricingToTransactionResponse(
            String externalTxId,

            String feePolicyId,
            Money fee,

            String taxPolicyId,
            Money tax,

            String ledgerTransactionId,
            boolean recorded
    ) {}
}
