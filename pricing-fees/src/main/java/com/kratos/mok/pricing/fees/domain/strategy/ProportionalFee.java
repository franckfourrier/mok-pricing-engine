package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.fees.domain.vo.FeePercentage;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ProportionalFee(FeePercentage percentage) implements FeeStrategy {

    @Override
    public Money apply(Money transactionAmount) {
        BigDecimal factor = percentage.value()
                .divide(new BigDecimal("100"), 10, RoundingMode.HALF_EVEN);

        return transactionAmount.multiply(factor);
    }

    @Override
    public FeeStrategyType type() {
        return FeeStrategyType.PROPORTIONAL;
    }
}

