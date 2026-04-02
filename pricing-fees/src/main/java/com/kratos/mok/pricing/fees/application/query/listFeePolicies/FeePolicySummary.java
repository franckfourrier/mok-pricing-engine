package com.kratos.mok.pricing.fees.application.query.listFeePolicies;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.List;

public record FeePolicySummary(
        String id,
        TransactionType transactionType,
        String TransactionLabel,
        String sender,
        String receiver,
        List<FeeTierSummary> tiers,
        String status,
        Integer priority,
        LocalDateTime validityStart,
        LocalDateTime validityEnd,
        LocalDateTime createdAt
) {}

