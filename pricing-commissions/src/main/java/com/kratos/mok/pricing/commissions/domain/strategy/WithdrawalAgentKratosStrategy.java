package com.kratos.mok.pricing.commissions.domain.strategy;

import com.kratos.mok.pricing.commissions.domain.enums.CommissionStrategyType;
import com.kratos.mok.pricing.commissions.domain.vo.Percentage;

import java.util.Objects;

public record WithdrawalAgentKratosStrategy(
        Percentage agentShare,
        Percentage coverageRate
) implements CommissionStrategy {

    public WithdrawalAgentKratosStrategy {
        Objects.requireNonNull(agentShare, "agentShare is required");
        Objects.requireNonNull(coverageRate, "coverageRate is required");
    }

    @Override
    public CommissionStrategyType type() {
        return CommissionStrategyType.WITHDRAWAL_AGENT_KRATOS;
    }

    public Percentage kratosShare() {
        // %Kratos = 1 - %Agent - %Coverage
        return Percentage.of(
                Percentage.ONE.value()
                        .subtract(agentShare.value())
                        .subtract(coverageRate.value())
        );
    }
}
