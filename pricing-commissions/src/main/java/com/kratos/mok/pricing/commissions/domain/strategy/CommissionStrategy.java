package com.kratos.mok.pricing.commissions.domain.strategy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kratos.mok.pricing.commissions.domain.enums.CommissionStrategyType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DepositDistributionStrategy.class, name = "DEPOSIT_DISTRIBUTION"),
        @JsonSubTypes.Type(value = WithdrawalAgentKratosStrategy.class, name = "WITHDRAWAL_AGENT_KRATOS"),
        @JsonSubTypes.Type(value = DirectStrategy.class, name = "DIRECT")
})
public sealed interface CommissionStrategy
        permits DepositDistributionStrategy, WithdrawalAgentKratosStrategy, DirectStrategy {

    CommissionStrategyType type();
}
