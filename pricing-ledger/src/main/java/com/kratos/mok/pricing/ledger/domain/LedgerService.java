package com.kratos.mok.pricing.ledger.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface LedgerService {
    void recordIfAbsent(
            String externalTxId,
            LocalDateTime occurredAt,
            String actor,
            List<LedgerPosting> postings
    );
}
