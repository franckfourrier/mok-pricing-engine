package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record FixedFee(Money amount) implements FeeStrategy {
    @Override
    public Money apply(Money transactionAmount) {
        return amount;
    }
}
