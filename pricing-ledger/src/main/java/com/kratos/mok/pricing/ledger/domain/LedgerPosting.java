package com.kratos.mok.pricing.ledger.domain;

import com.kratos.mok.pricing.ledger.domain.enums.EntryDirection;
import com.kratos.mok.pricing.ledger.domain.enums.LedgerEntryKind;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.util.Objects;

public record LedgerPosting(
        String accountCode,
        EntryDirection direction,
        Money amount,
        LedgerEntryKind kind,
        String policyId,     // nullable
        String description   // nullable
) {
    public LedgerPosting {
        Objects.requireNonNull(accountCode, "accountCode must not be null");
        Objects.requireNonNull(direction, "direction must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(kind, "kind must not be null");
        if (accountCode.isBlank()) throw new IllegalArgumentException("accountCode must not be blank");
    }

    public static LedgerPosting debit(String accountCode, Money amount, LedgerEntryKind kind, String policyId, String description) {
        return new LedgerPosting(accountCode, EntryDirection.DEBIT, amount, kind, policyId, description);
    }

    public static LedgerPosting credit(String accountCode, Money amount, LedgerEntryKind kind, String policyId, String description) {
        return new LedgerPosting(accountCode, EntryDirection.CREDIT, amount, kind, policyId, description);
    }
}

