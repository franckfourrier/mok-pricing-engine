package com.kratos.mok.pricing.fees.domain.snapshot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FeePolicySnapshot(
        String id,
        String transactionType,
        String targetScope,      // GLOBAL, PROFILE, INDIVIDUAL
        String targetValue,      // "ALL", "PREMIUM", "USER-123"
        String strategyType,     // FIXED, PROPORTIONAL...
        BigDecimal strategyValue,// Le montant ou le pourcentage
        String limitsMin,
        String limitsMax,
        String activationThreshold,
        boolean kycRequired,
        String status,
        String createdBy,
        LocalDateTime createdDate,
        String lastModifiedBy,
        LocalDateTime lastModifiedDate)
{}
