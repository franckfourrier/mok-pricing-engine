package com.kratos.mok.pricing.commissions.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Percentage(BigDecimal value) {

    public Percentage {
        Objects.requireNonNull(value, "percentage value is required");
        value = value.setScale(6, RoundingMode.HALF_EVEN);

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("percentage must be >= 0");
        }
        if (value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("percentage must be <= 1.0");
        }
    }

    public static Percentage of(double v) { return new Percentage(BigDecimal.valueOf(v)); }
    public static Percentage of(BigDecimal v) { return new Percentage(v); }
}
