package com.kratos.mok.pricing.app.application.command.bankDeposit;

import com.kratos.mok.pricing.app.infrastructure.repository.cantonment.CantonmentEntity;
import com.kratos.mok.pricing.app.infrastructure.repository.cantonment.JpaCantonmentRepository;
import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionCommand;
import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionResponse;
import com.kratos.mok.pricing.ledger.application.port.LedgerWriter;
import com.kratos.mok.pricing.ledger.domain.Posting;
import com.kratos.mok.pricing.ledger.domain.enums.EntryDirection;
import com.kratos.mok.pricing.ledger.domain.enums.LedgerEntryKind;
import com.kratos.mok.pricing.ledger.domain.event.LedgerEntriesCreatedEvent;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankDepositCommandHandler {

    private final LedgerWriter ledgerWriter;
    private final JpaCantonmentRepository cantonmentRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeProvider timeProvider;

    @Value("${ledger.accounts.cantonment:ACC-CANT}")
    private String accCant;

    @Value("${ledger.accounts.bankClearing:ACC-BANK-CLEAR}")
    private String accBankClearing;

    @Transactional
    public BankDepositResponse handle(BankDepositCommand cmd, String actor) {
        validate(cmd);

        String ref = cmd.referencePayment().trim();
        String externalTxId = ref;
        OffsetDateTime now = timeProvider.now();

        // 1) idempotence DB
        if (cantonmentRepo.existsByPaymentReference(ref)) {
            log.info("Duplicate cantonnement credit: ref={}", ref);
            return new BankDepositResponse(
                    ref, false, "DUPLICATE", externalTxId, accCant, cmd.amount()
            );
        }

        // 2) enregistrer RECEIVED (protège concurrence)
        var entity = new CantonmentEntity();
        entity.setId(CantonmentCreditId.generate().value());
        entity.setPaymentReference(ref);
        entity.setAmount(cmd.amount().amount().toPlainString());
        entity.setCurrency(cmd.amount().currency());
        entity.setPartnerId(cmd.superDistributorId().trim());
        entity.setOccurredAt(cmd.occurredAt());
        entity.setStatus("RECEIVED");
        entity.setReceivedAt(now);

        try {
            cantonmentRepo.save(entity);
        } catch (DataIntegrityViolationException e) {
            // Deux requêtes concurrentes => unique constraint
            log.info("Duplicate cantonnement credit (unique constraint): ref={}", ref);
            return new BankDepositResponse(
                    ref, false, "DUPLICATE", externalTxId, accCant, cmd.amount()
            );
        }

        // 3) ledger double-entry
        Money amount = cmd.amount();

        List<Posting> postings = List.of(
                debit(accBankClearing, amount, LedgerEntryKind.BANK_DEPOSIT,
                        ref, "Cantonnement credit (clearing debit) ref=" + ref),
                credit(accCant, amount, LedgerEntryKind.BANK_DEPOSIT,
                        ref, "Cantonnement credit (cantonnement credit) ref=" + ref)
        );

        var ledgerCmd = new RecordLedgerTransactionCommand(
                externalTxId,
                cmd.occurredAt(),
                postings
        );

        RecordLedgerTransactionResponse ledgerRes = ledgerWriter.record(ledgerCmd, actor);

        // 4) marquer APPLIED
        entity.setStatus("APPLIED");
        entity.setLedgerExternalTxId(externalTxId);
        entity.setAppliedAt(now);
        cantonmentRepo.save(entity);

        eventPublisher.publishEvent(
                new LedgerEntriesCreatedEvent(
                        cmd.referencePayment(),
                        cmd.occurredAt()
                )
        );

        return new BankDepositResponse(
                ref,
                ledgerRes.recorded(),
                "APPLIED",
                externalTxId,
                accCant,
                amount
        );
    }

    private Posting debit(String accountCode, Money m, LedgerEntryKind kind, String ref, String desc) {
        return new Posting(accountCode, EntryDirection.DEBIT, m.amount().toPlainString(), m.currency(), kind, ref, desc);
    }

    private Posting credit(String accountCode, Money m, LedgerEntryKind kind, String ref, String desc) {
        return new Posting(accountCode, EntryDirection.CREDIT, m.amount().toPlainString(), m.currency(), kind, ref, desc);
    }

    private void validate(BankDepositCommand cmd) {
        if (cmd == null) throw new IllegalArgumentException("command is required");

        if (cmd.referencePayment() == null || cmd.referencePayment().isBlank()) {
            throw new DomainValidationException("REFERENCE_REQUIRED", "referenceVersement is required", Map.of());
        }
        if (cmd.amount() == null) {
            throw new DomainValidationException("AMOUNT_REQUIRED", "amount is required", Map.of());
        }
        if (cmd.amount().isZero() || cmd.amount().isNegative()) {
            throw new DomainValidationException("INVALID_AMOUNT", "amount must be > 0", Map.of("amount", cmd.amount().toString()));
        }
        if (cmd.superDistributorId() == null || cmd.superDistributorId().isBlank()) {
            throw new DomainValidationException("SUPER_DISTRIBUTOR_REQUIRED", "superDistributeur is required", Map.of());
        }
        if (cmd.occurredAt() == null) {
            throw new DomainValidationException("OCCURRED_AT_REQUIRED", "occurredAt is required", Map.of());
        }
        if (cmd.amount().currency() == null || cmd.amount().currency().isBlank()) {
            throw new DomainValidationException("CURRENCY_REQUIRED", "currency is required", Map.of());
        }
    }
}
