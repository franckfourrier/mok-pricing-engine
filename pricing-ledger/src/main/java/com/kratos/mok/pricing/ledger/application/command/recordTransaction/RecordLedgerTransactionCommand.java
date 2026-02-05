package com.kratos.mok.pricing.ledger.application.command.recordTransaction;

import com.kratos.mok.pricing.ledger.domain.Posting;
import java.time.OffsetDateTime;
import java.util.List;

public record RecordLedgerTransactionCommand(
        String externalTxId,
        OffsetDateTime occurredAt,
        List<Posting> postings
) {
}
