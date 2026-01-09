package com.kratos.mok.pricing.fees.domain.vo;

import java.math.BigDecimal;

public record FeePercentage(BigDecimal value) {

    public FeePercentage {
        if (value == null) {
            throw new IllegalArgumentException("Le pourcentage ne peut pas être null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Un pourcentage de frais doit être compris entre 0% et 100%");
        }
    }

    public static FeePercentage of(double value) {
        return new FeePercentage(BigDecimal.valueOf(value));
    }
}
