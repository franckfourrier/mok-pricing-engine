package com.kratos.mok.pricing.shared.infrastructure.config.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class MoneyMixin {

    @JsonIgnore
    abstract boolean isPositive();

    @JsonIgnore
    abstract boolean isNegative();

    @JsonIgnore
    abstract boolean isZero();
}