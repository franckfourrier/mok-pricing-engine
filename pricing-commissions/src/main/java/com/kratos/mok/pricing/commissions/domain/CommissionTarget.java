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
    }

    public static CommissionTarget global() {
        return new CommissionTarget(TargetScope.GLOBAL, "ALL");
    }
}
