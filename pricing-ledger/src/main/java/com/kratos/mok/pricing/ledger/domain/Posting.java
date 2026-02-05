package com.kratos.mok.pricing.ledger.domain;

import com.kratos.mok.pricing.ledger.domain.enums.EntryDirection;
import com.kratos.mok.pricing.ledger.domain.enums.LedgerEntryKind;

public record Posting(
        String accountCode,
        EntryDirection direction,
        String amount,
        String currency,
        LedgerEntryKind kind,
        String policyId,
        String description
) {}