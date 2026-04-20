package com.kratos.mok.pricing.taxes.domain.vo;

import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public record TaxRate(BigDecimal value) {

    public TaxRate {
        Objects.requireNonNull(value, "rate value must not be null");

        // ]0, 100]
        if (value.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(new BigDecimal("100")) > 0) {
            throw new DomainValidationException(
                    "INVALID_TAX_RATE",
                    "Tax rate must be in ]0,100] (e.g. 1.9 for 1.9%)",
                    Map.of("rate", value)
            );
        }
    }

    public static TaxRate of(BigDecimal percent) {
        return new TaxRate(percent);
    }

    public BigDecimal asFraction() {
        return value.divide(new BigDecimal("100"));
    }
}
