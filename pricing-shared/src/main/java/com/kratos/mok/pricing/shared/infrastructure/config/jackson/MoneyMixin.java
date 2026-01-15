package com.kratos.mok.pricing.shared.infrastructure.config.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class MoneyMixin {

    @JsonIgnore
    abstract boolean isPositive();

    @JsonIgnore
    abstract boolean isNegative();

    @JsonIgnore
    abstract boolean isZero();
}