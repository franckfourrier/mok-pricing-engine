package com.kratos.mok.pricing.shared.api;

import java.time.OffsetDateTime;

public record PricingComputeRequest(
        String transactionType,
        String amount,
        String currency,
        String accountId,
        String accountType,
        boolean kycValidated,
        int monthlyTxCount,
        OffsetDateTime occurredAt
) {}
