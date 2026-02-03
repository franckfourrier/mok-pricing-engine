package com.kratos.mok.pricing.taxes.domain.vo;

import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public record TaxRate(BigDecimal value) {

    public TaxRate {
        Objects.requireNonNull(value, "rate value must not be null");

        // V1: 0..1
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new DomainValidationException(
                    "INVALID_TAX_RATE",
                    "Tax rate must be between 0 and 1 (e.g. 0.019 for 1.9%)",
                    Map.of("rate", value)
            );
        }
    }

    public static TaxRate of(BigDecimal v) {
        return new TaxRate(v);
    }
}
