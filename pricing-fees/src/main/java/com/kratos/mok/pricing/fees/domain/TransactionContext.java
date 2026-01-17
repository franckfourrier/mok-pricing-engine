package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.LocalDateTime;

public record TransactionContext(
        Money amount,
        LocalDateTime transactionDate,
        boolean isKycValidated,
        int monthlyTransactionCount
) {
    public static TransactionContext simple(Money amount) {
        return new TransactionContext(amount, LocalDateTime.now(), true, 0);
    }
}
