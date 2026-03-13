package com.kratos.mok.pricing.shared.domain.vo;

import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import java.time.OffsetDateTime;
import java.util.Objects;

public record PricingRequestContext(
        TransactionCode transactionCode,
        Money amount,
        String accountId,
        AccountType accountType,
        boolean kycValidated,
        int monthlyTxCount,
        OffsetDateTime occurredAt
) {
    public PricingRequestContext {
        Objects.requireNonNull(transactionCode, "transactionCode");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(accountType, "accountType");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}