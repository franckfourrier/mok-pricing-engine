package com.kratos.mok.pricing.ledger.domain;

import com.kratos.mok.pricing.ledger.domain.enums.EntryDirection;
import com.kratos.mok.pricing.ledger.domain.enums.LedgerEntryKind;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;

public record LedgerEntry(
        String externalTxId,
        int lineNo,
        OffsetDateTime occurredAt,
        String accountCode,
        EntryDirection direction,
        Money amount,
        LedgerEntryKind kind,
        String policyId,
        String description,
        String createdBy,
        LocalDateTime createdAt
) {
    public LedgerEntry {
        Objects.requireNonNull(externalTxId);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(accountCode);
        Objects.requireNonNull(direction);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(createdBy);
        Objects.requireNonNull(createdAt);

        if (externalTxId.isBlank()) throw new IllegalArgumentException("externalTxId must not be blank");
        if (accountCode.isBlank()) throw new IllegalArgumentException("accountCode must not be blank");
        if (amount.amount().signum() < 0) throw new IllegalArgumentException("amount must be >= 0");
    }
}

