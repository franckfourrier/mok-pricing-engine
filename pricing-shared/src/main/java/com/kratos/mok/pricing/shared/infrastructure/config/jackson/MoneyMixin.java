package com.kratos.mok.pricing.shared.infrastructure.config.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({
        "positive", "negative", "zero",
        "isPositive", "isNegative", "isZero",
        "getPositive", "getNegative", "getZero"
})
public abstract class MoneyMixin {

    @JsonIgnore
    abstract boolean isPositive();

    @JsonIgnore
    abstract boolean isNegative();

    @JsonIgnore
    abstract boolean isZero();
}