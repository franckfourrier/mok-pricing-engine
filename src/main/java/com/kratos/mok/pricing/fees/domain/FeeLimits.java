package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record FeeLimits(Money minAmount, Money maxAmount) {

    public static final FeeLimits NONE = new FeeLimits(null, null);

    public Money apply(Money calculatedFee) {
        Money result = calculatedFee;
        // Plancher (Minimum à payer si le frais n'est pas nul)
        if (minAmount != null && result.compareTo(minAmount) < 0) {
            result = minAmount;
        }
        // Plafond (Maximum à payer)
        if (maxAmount != null && result.compareTo(maxAmount) > 0) {
            result = maxAmount;
        }
        return result;
    }
}
