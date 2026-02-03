package com.kratos.mok.pricing.taxes.domain.strategy;

import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.taxes.domain.vo.FluxIntensity;

import java.util.Map;

public record TaxRules(
        FluxIntensity fluxIntensity,
        boolean exempted
) {

    public TaxRules {
        if (fluxIntensity != null && fluxIntensity.value().compareTo(FluxIntensity.defaultOne().value()) < 0) {
            throw new DomainValidationException(
                    "INVALID_FLUX_INTENSITY",
                    "Flux intensity must be >= 1.0",
                    Map.of("intensity", fluxIntensity.value())
            );
        }
    }

    public FluxIntensity intensity() {
        return fluxIntensity != null ? fluxIntensity : FluxIntensity.defaultOne();
    }

    public boolean isExempted() {
        return exempted;
    }

    public static TaxRules standard() {
        return new TaxRules(null, false);
    }

    public static TaxRules withIntensity(FluxIntensity intensity) {
        return new TaxRules(intensity, false);
    }

    public static TaxRules exemptedWithIntensity(FluxIntensity intensity) {
        return new TaxRules(intensity, true);
    }
}
