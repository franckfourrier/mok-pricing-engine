package com.kratos.mok.pricing.taxes.domain.vo;

import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public record FluxIntensity(BigDecimal value) {

    public FluxIntensity {
        Objects.requireNonNull(value, "intensity must not be null");

        /*if (value.compareTo(BigDecimal.ONE) < 0) {
            throw new DomainValidationException(
                    "INVALID_FLUX_INTENSITY",
                    "Flux intensity must be >= 1.0",
                    Map.of("intensity", value)
            );
        }*/

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException(
                    "INVALID_FLUX_INTENSITY",
                    "Flux intensity must be > 0",
                    Map.of("intensity", value)
            );
        }
    }

    public static FluxIntensity defaultOne() {
        return new FluxIntensity(BigDecimal.ONE);
    }
}
