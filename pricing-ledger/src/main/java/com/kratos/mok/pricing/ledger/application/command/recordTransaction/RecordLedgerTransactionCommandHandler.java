package com.kratos.mok.pricing.ledger.application.command.recordTransaction;


import com.kratos.mok.pricing.ledger.domain.LedgerEntry;
import com.kratos.mok.pricing.ledger.domain.repository.LedgerAccountRepository;
import com.kratos.mok.pricing.ledger.domain.repository.LedgerEntryRepository;
import com.kratos.mok.pricing.ledger.domain.vo.AccountCode;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RecordLedgerTransactionCommandHandler {

    private final LedgerEntryRepository entryRepo;
    private final LedgerAccountRepository accountRepo;

    @Transactional
    public RecordLedgerTransactionResponse handle(RecordLedgerTransactionCommand cmd, String actor) {

        if (cmd == null) throw new IllegalArgumentException("cmd is required");
        if (cmd.externalTxId() == null || cmd.externalTxId().isBlank()) throw new IllegalArgumentException("externalTxId is required");
        if (cmd.occurredAt() == null) throw new IllegalArgumentException("occurredAt is required");
        if (cmd.postings() == null || cmd.postings().isEmpty()) throw new IllegalArgumentException("postings must not be empty");

        String ext = cmd.externalTxId().trim();
        if (entryRepo.existsByExternalTxId(ext)) {
            return new RecordLedgerTransactionResponse(ext, false); // idempotent NO-OP
        }

        String by = (actor == null || actor.isBlank()) ? "SYSTEM" : actor.trim();
        LocalDateTime now = LocalDateTime.now();

        List<LedgerEntry> entries = IntStream.range(0, cmd.postings().size())
                .mapToObj(i -> {
                    var p = cmd.postings().get(i);
                    int lineNo = i + 1;

                    AccountCode code = AccountCode.of(p.accountCode());
                    if (!accountRepo.existsByCode(code)) {
                        throw new IllegalStateException("Ledger account not found: " + code.value());
                    }

                    Money m = Money.of(p.amount(), p.currency());

                    return new LedgerEntry(
                            ext,
                            lineNo,
                            cmd.occurredAt(),
                            code.value(),
                            p.direction(),
                            m,
                            p.kind(),
                            p.policyId(),
                            p.description(),
                            by,
                            now
                    );
                })
                .toList();


        entryRepo.appendAll(entries);
        return new RecordLedgerTransactionResponse(ext, true);
    }
}

