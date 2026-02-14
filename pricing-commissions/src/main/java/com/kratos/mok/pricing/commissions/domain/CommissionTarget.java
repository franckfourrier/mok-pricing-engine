package com.kratos.mok.pricing.commissions.domain;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;

import java.util.Objects;

public record CommissionTarget(TargetScope scope, String value) {

    public CommissionTarget {
        Objects.requireNonNull(scope, "scope is required");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("target value is required");
        }
        value = value.trim();
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

    public static CommissionTarget global() {
        return new CommissionTarget(TargetScope.GLOBAL, "ALL");
    }

    public static CommissionTarget accountType(String accountType) {
        Objects.requireNonNull(accountType, "accountType must not be null");
        return new CommissionTarget(TargetScope.ACCOUNT_TYPE, accountType.trim().toUpperCase());
    }

    public static CommissionTarget accountId(String accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return new CommissionTarget(TargetScope.ACCOUNT_ID, accountId.trim());
    }
}
