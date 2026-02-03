package com.kratos.mok.pricing.taxes.domain.strategy;

import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import com.kratos.mok.pricing.taxes.domain.vo.FluxIntensity;

import java.math.BigDecimal;
import java.util.Map;

public final class FixedAmountTax implements TaxStrategy {

    private final Money fixed;

    public FixedAmountTax(Money fixed) {
        if (fixed == null) {
            throw new DomainValidationException("FIXED_TAX_REQUIRED", "Fixed tax amount is required", Map.of());
        }
        if (fixed.amount() == null || fixed.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException(
                    "INVALID_FIXED_TAX",
                    "Fixed tax amount must be >= 0",
                    Map.of("amount", fixed.amount())
            );
        }
        this.fixed = fixed;
    }

    public Money fixed() { return fixed; }

    @Override public TaxStrategyType type() { return TaxStrategyType.FIXED_AMOUNT; }

    @Override
    public Money apply(Money baseAmount, FluxIntensity intensity) {
        BigDecimal raw = fixed.amount().multiply(intensity.value());
        return Money.of(raw, fixed.currency());
    }
}
