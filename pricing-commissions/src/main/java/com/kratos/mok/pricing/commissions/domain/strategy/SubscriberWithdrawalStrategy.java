package com.kratos.mok.pricing.commissions.domain.strategy;

import com.kratos.mok.pricing.commissions.domain.enums.CommissionStrategyType;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;
import com.kratos.mok.pricing.commissions.domain.vo.Percentage;

import java.util.List;
import java.util.Objects;

public record SubscriberWithdrawalStrategy (List<CommissionShare> keys) implements CommissionStrategy {

    public SubscriberWithdrawalStrategy {
        Objects.requireNonNull(keys, "keys is required");
        if (keys.isEmpty()) throw new IllegalArgumentException("keys must not be empty");
    }

    @Override
    public CommissionStrategyType type() {
        return CommissionStrategyType.SUBSCRIBER_WITHDRAWAL;
    }

    /*public Percentage kratosShare() {
        // %Kratos = 1 - %Agent - %Coverage
        return Percentage.of(
                Percentage.ONE.value()
                        .subtract(agentShare.value())
                        .subtract(coverageRate.value())
        );
    }*/
}
