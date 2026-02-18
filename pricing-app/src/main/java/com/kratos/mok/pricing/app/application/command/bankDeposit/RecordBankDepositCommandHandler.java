package com.kratos.mok.pricing.app.application.command.bankDeposit;

import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionCommand;
import com.kratos.mok.pricing.ledger.application.port.LedgerWriter;
import com.kratos.mok.pricing.ledger.domain.Posting;
import com.kratos.mok.pricing.ledger.domain.enums.EntryDirection;
import com.kratos.mok.pricing.ledger.domain.enums.LedgerEntryKind;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecordBankDepositCommandHandler {

    private final LedgerWriter ledgerWriter;

    @Value("${ledger.accounts.cantonnement:ACC-CANT}")
    private String accCant;

    // compte technique interne pour équilibrer
    @Value("${ledger.accounts.bankClearing:ACC-BANK-CLEAR}")
    private String accBankClearing;

    @Value("${mok.currency.default:XAF}")
    private String defaultCurrency;

    @Transactional
    public RecordBankDepositResponse handle(RecordBankDepositCommand cmd, String actor) {
        validate(cmd);

        // Idempotency key : la référence banque est la clé “métier”
        String externalTxId = "BANK_DEPOSIT:" + cmd.referenceVersement().trim();

        Money amount = Money.of(String.valueOf(cmd.montant()), defaultCurrency);

        // Ecritures ledger (double-entry)
        List<Posting> postings = List.of(
                debit(accBankClearing, amount, LedgerEntryKind.BANK_DEPOSIT,
                        cmd.referenceVersement(), "Bank deposit debit (clearing) ref=" + cmd.referenceVersement()),
                credit(accCant, amount, LedgerEntryKind.BANK_DEPOSIT,
                        cmd.referenceVersement(), "Bank deposit credit to cantonnement ref=" + cmd.referenceVersement())
        );

        var ledgerCmd = new RecordLedgerTransactionCommand(
                externalTxId,
                OffsetDateTime.now(),
                postings
        );

        var ledgerRes = ledgerWriter.record(ledgerCmd, actor);

        return new RecordBankDepositResponse(
                cmd.referenceVersement(),
                ledgerRes.recorded(),
                accCant,
                amount
        );
    }

    private Posting debit(String accountCode, Money m, LedgerEntryKind kind, String policyId, String desc) {
        return new Posting(accountCode, EntryDirection.DEBIT, m.amount().toPlainString(), m.currency(), kind, policyId, desc);
    }

    private Posting credit(String accountCode, Money m, LedgerEntryKind kind, String policyId, String desc) {
        return new Posting(accountCode, EntryDirection.CREDIT, m.amount().toPlainString(), m.currency(), kind, policyId, desc);
    }

    private void validate(RecordBankDepositCommand cmd) {
        if (cmd == null) throw new IllegalArgumentException("command is required");
        if (cmd.referenceVersement() == null || cmd.referenceVersement().isBlank()) {
            throw new DomainValidationException("REFERENCE_REQUIRED", "referenceVersement is required", Map.of());
        }
        if (cmd.montant() <= 0) {
            throw new DomainValidationException("INVALID_AMOUNT", "montant must be > 0", Map.of("montant", cmd.montant()));
        }
        if (cmd.superDistributeurId() == null || cmd.superDistributeurId().isBlank()) {
            throw new DomainValidationException("SUPER_DISTRIBUTOR_REQUIRED", "superDistributeur is required", Map.of());
        }
    }
}
