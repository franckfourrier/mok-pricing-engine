package com.kratos.mok.pricing.taxes.application.query.listTaxPolicies;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;

import java.time.LocalDateTime;

public record TaxPolicySummary(
         /*SString id,
        String name,
        TransactionType transactionType,
        TargetScope targetScope,
        String targetValue,
        String taxMode,
        String strategyType,
        String value,
        String status,
        LocalDateTime createdAt*/

       String id,
       String name,
       String appliedTransaction,
       String type,
       String value,
       TransactionType transactionType,
       TargetScope targetScope,
       String targetValue,
       String status,
       String statusLabel,
       String createdAt
) {}
