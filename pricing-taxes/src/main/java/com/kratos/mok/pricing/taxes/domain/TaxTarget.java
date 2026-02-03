package com.kratos.mok.pricing.taxes.domain;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;

import java.util.Map;

public record TaxTarget(TargetScope scope, String value) {

    public TaxTarget {
        if (scope == null) throw new DomainValidationException("TARGET_SCOPE_REQUIRED", "target scope is required", Map.of());
        if (value == null || value.isBlank()) throw new DomainValidationException("TARGET_VALUE_REQUIRED", "target value is required", Map.of());

        if (scope == TargetScope.GLOBAL && !"ALL".equalsIgnoreCase(value.trim())) {
            throw new DomainValidationException("INVALID_GLOBAL_TARGET", "GLOBAL targetValue must be ALL", Map.of("value", value));
        }
    }

    public static TaxTarget global() { return new TaxTarget(TargetScope.GLOBAL, "ALL"); }
    public static TaxTarget accountType(String type) { return new TaxTarget(TargetScope.ACCOUNT_TYPE, type.trim().toUpperCase()); }
    public static TaxTarget accountId(String id) { return new TaxTarget(TargetScope.ACCOUNT_ID, id.trim()); }
}
