package com.kratos.mok.pricing.ledger.application.query.getLastEntries;

import com.kratos.mok.pricing.ledger.domain.repository.LedgerEntryRepository;
import com.kratos.mok.pricing.ledger.domain.vo.AccountCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetLastEntriesQueryHandler {

    private final LedgerEntryRepository entries;

    @Transactional(readOnly = true)
    public List<LedgerEntryView> handle(GetLastEntriesQuery q) {
        if (q.accountCode() == null || q.accountCode().isBlank()) throw new IllegalArgumentException("accountCode is required");
        int limit = (q.limit() <= 0) ? 20 : Math.min(q.limit(), 200);

        AccountCode code = AccountCode.of(q.accountCode());

        return entries.lastEntries(code, limit).stream()
                .map(e -> new LedgerEntryView(
                        e.externalTxId(),
                        e.occurredAt(),
                        e.direction().name(),
                        e.amount(),
                        e.kind().name(),
                        e.policyId(),
                        e.description()
                ))
                .toList();
    }
}
