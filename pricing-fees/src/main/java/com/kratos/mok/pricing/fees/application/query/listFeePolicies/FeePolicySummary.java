package com.kratos.mok.pricing.fees.application.query.listFeePolicies;

import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import java.time.OffsetDateTime;
import java.util.List;

public record FeePolicySummary(
        String id,
        String shortId,
        TransactionType transactionType,
        String transactionLabel,
        String sender,
        String receiver,
        List<FeeTierSummary> tiers,
        String status,
        Integer priority,
        OffsetDateTime validityStart,
        OffsetDateTime validityEnd,
        OffsetDateTime createdAt
) {}

