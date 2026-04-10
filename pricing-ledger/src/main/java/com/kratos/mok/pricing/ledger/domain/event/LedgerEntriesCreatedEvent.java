package com.kratos.mok.pricing.ledger.domain.event;

import java.time.OffsetDateTime;

public record LedgerEntriesCreatedEvent(
        String externalTxId,
        OffsetDateTime occurredAt
) {}