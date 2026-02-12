package com.kratos.mok.pricing.commissions.domain.strategy;

import com.kratos.mok.pricing.commissions.domain.enums.CommissionStrategyType;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionKey;
import com.kratos.mok.pricing.commissions.domain.vo.Percentage;

import java.util.List;
import java.util.Objects;

public record WithdrawalCompensationStrategy(
        Percentage coverageRate,
        List<CommissionKey> coverageKeys,
        List<CommissionKey> surplusKeys
) implements CommissionStrategy {

    public WithdrawalCompensationStrategy {
        Objects.requireNonNull(coverageRate, "coverageRate is required");
        Objects.requireNonNull(coverageKeys, "coverageKeys is required");
        Objects.requireNonNull(surplusKeys, "surplusKeys is required");
        if (coverageKeys.isEmpty()) throw new IllegalArgumentException("coverageKeys must not be empty");
        if (surplusKeys.isEmpty()) throw new IllegalArgumentException("surplusKeys must not be empty");
    }

    @Override public CommissionStrategyType type() { return CommissionStrategyType.WITHDRAWAL_COMPENSATION; }
}
