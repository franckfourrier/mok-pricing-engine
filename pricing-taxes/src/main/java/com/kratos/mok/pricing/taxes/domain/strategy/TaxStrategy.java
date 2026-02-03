package com.kratos.mok.pricing.taxes.domain.strategy;

import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import com.kratos.mok.pricing.taxes.domain.vo.FluxIntensity;
import com.kratos.mok.pricing.shared.domain.vo.Money;

public sealed interface TaxStrategy permits ElectronicRateTax, FixedAmountTax {
    TaxStrategyType type();
    Money apply(Money baseAmount, FluxIntensity intensity);
}
