package com.kratos.mok.pricing.app.infrastructure.config.fees;

import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;

public abstract class FeePercentageMixin {
    @JsonValue
    abstract BigDecimal value();
}