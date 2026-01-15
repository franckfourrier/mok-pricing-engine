package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record FixedFee(Money feeAmount) implements FeeStrategy {

    public FixedFee {
        if (feeAmount == null) {
            throw new IllegalArgumentException("Le montant du frais fixe est requis");
        }
        // Vérification Taux Positifs
        if (!feeAmount.isPositive() && !feeAmount.equals(Money.ZERO)) {
            // On accepte 0 (gratuit), mais pas négatif
            throw new IllegalArgumentException("Un frais fixe ne peut pas être négatif");
        }
    }

    @Override
    public Money apply(Money transactionAmount) {
        return feeAmount;
    }

    @Override
    public FeeStrategyType type() {
        return FeeStrategyType.FIXED;
    }
}
