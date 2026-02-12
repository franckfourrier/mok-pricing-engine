package com.kratos.mok.pricing.commissions.domain.strategy;

import com.kratos.mok.pricing.commissions.domain.enums.CommissionStrategyType;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionKey;

import java.util.List;
import java.util.Objects;

public record DepositDeferredStrategy(List<CommissionKey> keys) implements CommissionStrategy {
    public DepositDeferredStrategy {
        Objects.requireNonNull(keys, "keys is required");
        if (keys.isEmpty()) throw new IllegalArgumentException("keys must not be empty");
    }
    @Override public CommissionStrategyType type() { return CommissionStrategyType.DEPOSIT_DEFERRED; }
}
