package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Contexte minimal nécessaire au calcul d'un frais.
 *
 * Utilisé uniquement par le domaine Fees.
 */
public record TransactionContext(
        Money amount,
        LocalDateTime occurredAt,
        String accountId,
        AccountType accountType,
        boolean kycValidated,
        int monthlyTransactionCount
) {

    public TransactionContext {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(accountType, "accountType must not be null");

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be blank");
        }
        if (monthlyTransactionCount < 0) {
            throw new IllegalArgumentException("monthlyTransactionCount cannot be negative");
        }
    }

    public boolean isKycValidated() {
        return kycValidated;
    }
}
