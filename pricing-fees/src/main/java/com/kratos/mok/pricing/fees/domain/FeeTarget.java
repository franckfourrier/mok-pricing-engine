package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.fees.domain.enums.TargetScope;

import java.util.Objects;

public record FeeTarget(TargetScope scope, String value) {

    public FeeTarget {
        Objects.requireNonNull(scope, "scope must not be null");
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("FeeTarget.value cannot be blank");
        }

        switch (scope) {
            case GLOBAL -> {
                if (!"ALL".equals(value)) {
                    throw new IllegalArgumentException("GLOBAL scope requires value = 'ALL'");
                }
            }
            case ACCOUNT_TYPE -> {
                if ("ALL".equals(value)) {
                    throw new IllegalArgumentException("ACCOUNT_TYPE scope cannot use value 'ALL'");
                }
            }
            case ACCOUNT_ID -> {
                if ("ALL".equals(value)) {
                    throw new IllegalArgumentException("ACCOUNT_ID scope cannot use value 'ALL'");
                }
            }
        }
    }

    public static FeeTarget global() {
        return new FeeTarget(TargetScope.GLOBAL, "ALL");
    }

    public static FeeTarget accountType(String accountType) {
        Objects.requireNonNull(accountType, "accountType must not be null");
        return new FeeTarget(TargetScope.ACCOUNT_TYPE, accountType.trim().toUpperCase());
    }

    public static FeeTarget accountId(String accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return new FeeTarget(TargetScope.ACCOUNT_ID, accountId.trim());
    }
}
