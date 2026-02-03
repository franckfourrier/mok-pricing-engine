package com.kratos.mok.pricing.taxes.domain.strategy;

import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import com.kratos.mok.pricing.taxes.domain.vo.FluxIntensity;
import com.kratos.mok.pricing.taxes.domain.vo.TaxRate;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.math.BigDecimal;
import java.util.Map;

public final class ElectronicRateTax implements TaxStrategy {

    private final TaxRate rate;

    public ElectronicRateTax(TaxRate rate) {
        if (rate == null) throw new DomainValidationException("TAX_RATE_REQUIRED", "Tax rate is required", Map.of());
        this.rate = rate;
    }

    public TaxRate rate() {
        return rate;
    }

    @Override public TaxStrategyType type() {
        return TaxStrategyType.ELECTRONIC_RATE;
    }

    @Override
    public Money apply(Money baseAmount, FluxIntensity intensity) {
        BigDecimal raw = baseAmount.amount()
                .multiply(rate.value())
                .multiply(intensity.value());
        return Money.of(raw, baseAmount.currency());
    }
}
