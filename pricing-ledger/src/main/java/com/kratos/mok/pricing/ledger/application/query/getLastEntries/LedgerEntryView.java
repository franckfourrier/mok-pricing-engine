package com.kratos.mok.pricing.ledger.application.query.getLastEntries;

import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.OffsetDateTime;

public record LedgerEntryView(
        String externalTxId,
        OffsetDateTime occurredAt,
        String direction,
        Money amount,
        String kind,
        String policyId,
        String description
) {}
