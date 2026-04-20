package com.kratos.mok.pricing.commissions.domain.strategy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kratos.mok.pricing.commissions.domain.enums.CommissionStrategyType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SubscriberDepositStrategy.class, name = "SUBSCRIBER_DEPOSIT"),
        @JsonSubTypes.Type(value = SubscriberWithdrawalStrategy.class, name = "SUBSCRIBER_WITHDRAWAL"),
        @JsonSubTypes.Type(value = DirectStrategy.class, name = "DIRECT")
})
public sealed interface CommissionStrategy
        permits SubscriberDepositStrategy, SubscriberWithdrawalStrategy, DirectStrategy {

    CommissionStrategyType type();
}
