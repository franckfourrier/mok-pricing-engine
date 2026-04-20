package com.kratos.mok.pricing.commissions.domain.strategy;

import com.kratos.mok.pricing.commissions.domain.enums.CommissionStrategyType;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;

import java.util.List;
import java.util.Objects;

public record SubscriberDepositStrategy(List<CommissionShare> keys) implements CommissionStrategy {

    public SubscriberDepositStrategy {
        Objects.requireNonNull(keys, "keys is required");
        if (keys.isEmpty()) throw new IllegalArgumentException("keys must not be empty");
    }

    @Override
    public CommissionStrategyType type() {
        return CommissionStrategyType.SUBSCRIBER_DEPOSIT;
    }
}
