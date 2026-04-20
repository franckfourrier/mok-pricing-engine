package com.kratos.mok.pricing.taxes.application.query.listTaxPolicies;

import java.util.List;

public record TaxPolicySummary(
        String id,
        String shortId,
        String name,
        List<String> appliedTransactions,
        String type,
        String value,
        /*TargetScope targetScope,
        String targetValue,*/
        String status,
        String statusLabel,
        String createdAt
) {}
