package com.kratos.mok.pricing.shared.api;

import java.time.OffsetDateTime;

public record PricingComputeRequest(
        String transactionCode,
        MoneyDto amount,
        String accountId,
        String accountType,
        boolean kycValidated,
        int monthlyTxCount,
        OffsetDateTime occurredAt
) {}
