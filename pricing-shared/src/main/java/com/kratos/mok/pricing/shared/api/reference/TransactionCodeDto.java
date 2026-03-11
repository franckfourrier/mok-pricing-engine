package com.kratos.mok.pricing.shared.api.reference;

public record TransactionCodeDto(
        String code,
        String label,
        String transactionType
) {
}
