package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.math.BigDecimal;

public record ProportionalFee(BigDecimal percentage) implements FeeStrategy {
    @Override
    public Money apply(Money transactionAmount) {
        return transactionAmount.multiply(percentage);
    }
}
