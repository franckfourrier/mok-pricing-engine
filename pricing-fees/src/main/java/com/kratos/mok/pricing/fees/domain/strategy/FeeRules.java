package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;
import java.util.Optional;

public record FeeRules(
        Money activationThreshold,
        Money minFee,
        Money maxFee,
        Integer minMonthlyTxCount
) {
    public FeeRules {
        if (minFee != null && minFee.isNegative()) {
            throw new IllegalArgumentException("minFee cannot be negative");
        }
        if (maxFee != null && maxFee.isNegative()) {
            throw new IllegalArgumentException("maxFee cannot be negative");
        }
        if (minFee != null && maxFee != null && minFee.compareTo(maxFee) > 0) {
            throw new IllegalArgumentException("minFee cannot be greater than maxFee");
        }
        if (activationThreshold != null && activationThreshold.isNegative()) {
            throw new IllegalArgumentException("activationThreshold cannot be negative");
        }
        if (minMonthlyTxCount != null && minMonthlyTxCount < 0) {
            throw new IllegalArgumentException("minMonthlyTxCount cannot be negative");
        }
    }

    public Money applyMinMax(Money fee) {
        if (fee == null) throw new IllegalArgumentException("fee cannot be null");

        Money result = fee;

        if (minFee != null && result.compareTo(minFee) < 0) {
            result = minFee;
        }
        if (maxFee != null && result.compareTo(maxFee) > 0) {
            result = maxFee;
        }
        return result;
    }

    public Optional<Integer> minMonthlyTxCount() {
        return Optional.ofNullable(minMonthlyTxCount);
    }
}
