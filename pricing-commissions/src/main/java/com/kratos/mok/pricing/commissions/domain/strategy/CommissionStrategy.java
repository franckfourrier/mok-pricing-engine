package com.kratos.mok.pricing.commissions.domain.strategy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kratos.mok.pricing.commissions.domain.enums.CommissionStrategyType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DepositDeferredStrategy.class, name = "DEPOSIT_DEFERRED"),
        @JsonSubTypes.Type(value = WithdrawalCompensationStrategy.class, name = "WITHDRAWAL_COMPENSATION"),
        @JsonSubTypes.Type(value = DirectStrategy.class, name = "DIRECT")
})
public sealed interface CommissionStrategy
        permits DepositDeferredStrategy, WithdrawalCompensationStrategy, DirectStrategy {

    CommissionStrategyType type();
}
