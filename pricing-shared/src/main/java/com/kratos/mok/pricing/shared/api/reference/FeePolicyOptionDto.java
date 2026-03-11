package com.kratos.mok.pricing.shared.api.reference;

public record FeePolicyOptionDto(
        String transactionCode,
        String transactionLabel,
        String transactionType,
        String targetScope,
        String targetValue,
        String targetValueLabel
) {}