package com.kratos.mok.pricing.shared.domain.vo;

import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

public record PricingRequestContext(
        TransactionCode transactionCode,
        Money amount,
        String accountId,
        AccountType accountType,
        boolean kycValidated,
        int monthlyTxCount,
        OffsetDateTime occurredAt,
        Map<String, String> hierarchy
) {
    public PricingRequestContext {
        Objects.requireNonNull(transactionCode, "transactionCode");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(accountType, "accountType");
        Objects.requireNonNull(occurredAt, "occurredAt");
        hierarchy = (hierarchy == null) ? Map.of() : hierarchy;
    }
    public PricingRequestContext(
            TransactionCode transactionCode,
            Money amount,
            String accountId,
            AccountType accountType,
            boolean kycValidated,
            int monthlyTxCount,
            OffsetDateTime occurredAt) {
        this(transactionCode, amount, accountId, accountType, kycValidated, monthlyTxCount, occurredAt, Map.of());
    }
        public String getAccountFor(String beneficiaryType) {
            return hierarchy.getOrDefault(beneficiaryType.toUpperCase(), accountId);
    }
}