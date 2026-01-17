package com.kratos.mok.pricing.fees.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;

public abstract class FeePercentageMixin {
    @JsonValue
    abstract BigDecimal value();
}