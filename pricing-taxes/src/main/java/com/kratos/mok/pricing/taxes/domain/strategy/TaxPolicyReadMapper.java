package com.kratos.mok.pricing.taxes.domain.strategy;

import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import com.kratos.mok.pricing.taxes.infrastructure.model.TaxPolicyEntity;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public final class TaxPolicyReadMapper {

    private TaxPolicyReadMapper() {}

    private static final DecimalFormat PCT_FMT = new DecimalFormat("#0.##"); // 0.05 -> "0,05"

    public static String value(TaxPolicyEntity e) {
        if (e.getStrategyType() == TaxStrategyType.FIXED_AMOUNT) {
            BigDecimal a = e.getFixedAmount() == null ? BigDecimal.ZERO : e.getFixedAmount();
            return a.toPlainString() + " " + e.getCurrency();
        }

        BigDecimal pct = e.getRate() == null ? BigDecimal.ZERO : e.getRate();
        return PCT_FMT.format(pct) + "%";
    }
}