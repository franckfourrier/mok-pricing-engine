package com.kratos.mok.pricing.fees.domain.strategy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kratos.mok.pricing.shared.domain.vo.Money;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FixedFee.class, name = "FIXED"),
        @JsonSubTypes.Type(value = ProportionalFee.class, name = "PROPORTIONAL")
})
public sealed interface FeeStrategy permits FixedFee, ProportionalFee, TieredFee {
    Money apply(Money transactionAmount);
}

