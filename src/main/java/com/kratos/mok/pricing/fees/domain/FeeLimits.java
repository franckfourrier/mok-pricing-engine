package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record FeeLimits(Money minAmount, Money maxAmount) {

    public static final FeeLimits NONE = new FeeLimits(null, null);

    public FeeLimits {
        // Validation Barrière 1 : Le Min ne peut pas dépasser le Max
        if (minAmount != null && maxAmount != null) {
            if (minAmount.compareTo(maxAmount) > 0) {
                throw new IllegalArgumentException("Le montant minimum (" + minAmount.amount() +
                        ") ne peut pas être supérieur au montant maximum (" + maxAmount.amount() + ").");
            }
        }
        // Validation : Pas de limites négatives
        if ((minAmount != null && !minAmount.isPositive() && !minAmount.equals(Money.ZERO)) ||
                (maxAmount != null && !maxAmount.isPositive() && !maxAmount.equals(Money.ZERO))) {
            throw new IllegalArgumentException("Les limites de frais ne peuvent pas être négatives.");
        }
    }

    public Money apply(Money calculatedFee) {
        Money result = calculatedFee;

        // 1. Application du Plancher (Min)
        // Règle : On applique le min SEULEMENT si le frais calculé n'est pas déjà gratuit (0).
        // Si vous voulez que le min s'applique même sur 0, retirez "!result.equals(Money.ZERO)"
        if (minAmount != null && !result.equals(Money.ZERO) && result.compareTo(minAmount) < 0) {
            result = minAmount;
        }

        // 2. Application du Plafond (Max)
        if (maxAmount != null && result.compareTo(maxAmount) > 0) {
            result = maxAmount;
        }

        return result;
    }
}
