package com.kratos.mok.pricing.fees.domain.snapshot;

public record FeePolicySnapshot(
        String id,
        String transactionType,
        String targetAccount,
        String strategy,
        String status,
        String createdBy,
        String lastModifiedBy) {
}
