package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record Tier(Money min, Money max, FeeStrategy strategy) {

    public Tier {
        if (min == null || max == null) {
            throw new IllegalArgumentException("Les bornes min et max du palier sont obligatoires.");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("La stratégie interne du palier est obligatoire.");
        }
        if (!min.isPositive() && !min.equals(Money.ZERO)) {
            throw new IllegalArgumentException("Le montant minimum d'un palier ne peut pas être négatif.");
        }
        // Validation Barrière 1 : Cohérence Min/Max
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Le minimum (" + min.amount() + ") ne peut pas être supérieur au maximum (" + max.amount() + ").");
        }
    }

    public boolean covers(Money amount) {
        // INCLUSIF sur le Min (>=) EXCLUSIF sur le Max (<)
        return amount.compareTo(min) >= 0 && amount.compareTo(max) < 0;
    }

    public Money calculate(Money amount) {

        return strategy.apply(amount);
    }
}
