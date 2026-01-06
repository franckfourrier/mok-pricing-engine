package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record Tier(Money min, Money max, FeeStrategy strategy) {
    public boolean covers(Money amount) {
        // Logique de comparaison (>= min et <= max)
        return amount.compareTo(min) >= 0 && amount.compareTo(max) <= 0;
    }

    public Money calculate(Money amount) {
        return strategy.apply(amount);
    }
}
