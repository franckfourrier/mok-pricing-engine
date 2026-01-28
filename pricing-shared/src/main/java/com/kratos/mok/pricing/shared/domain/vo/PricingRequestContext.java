package com.kratos.mok.pricing.shared.domain.vo;

import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import java.time.OffsetDateTime;
import java.util.Objects;

public record PricingRequestContext(
        TransactionType transactionType,
        Money amount,
        String accountId,
        AccountType accountType,
        boolean kycValidated,
        int monthlyTxCount,
        OffsetDateTime occurredAt
) {
    public PricingRequestContext {
        Objects.requireNonNull(transactionType, "transactionType");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(accountType, "accountType");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}