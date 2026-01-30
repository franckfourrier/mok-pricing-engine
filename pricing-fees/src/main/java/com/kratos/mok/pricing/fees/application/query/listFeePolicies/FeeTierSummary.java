package com.kratos.mok.pricing.fees.application.query.listFeePolicies;

import com.kratos.mok.pricing.shared.api.MoneyDto;

import java.math.BigDecimal;

public record FeeTierSummary(
        MoneyDto min,
        MoneyDto max,
        String strategyType,
        MoneyDto fixedAmount,
        BigDecimal percentage
) {}
