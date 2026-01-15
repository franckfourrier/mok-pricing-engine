package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.util.Comparator;
import java.util.List;

public record TieredFee(List<Tier> tiers) implements FeeStrategy {
    public TieredFee {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("Une stratégie par paliers doit contenir au moins un palier.");
        }

        // Validation Barrière 1 : Vérification des chevauchements
        validateTiersCoherence(tiers);
    }

    @Override
    public FeeStrategyType type() {
        return FeeStrategyType.TIERED;
    }


    private static void validateTiersCoherence(List<Tier> tiers) {
        List<Tier> sortedTiers = tiers.stream()
                .sorted(Comparator.comparing(Tier::min))
                .toList();

        Tier previousTier = null;
        for (Tier currentTier : sortedTiers) {
            if (previousTier != null) {
                if (currentTier.min().compareTo(previousTier.max()) < 0) {
                    throw new IllegalArgumentException(
                            String.format("Chevauchement réel détecté : Le palier commençant à %s mord sur le palier finissant à %s",
                                    currentTier.min().amount(), previousTier.max().amount())
                    );
                }
            }
            previousTier = currentTier;
        }
    }

    @Override
    public Money apply(Money transactionAmount) {
        return tiers.stream()
                .filter(t -> t.covers(transactionAmount))
                .findFirst()
                .map(t -> t.calculate(transactionAmount))
                .orElseThrow(() -> new IllegalArgumentException("Le montant " + transactionAmount.amount() + " ne correspond à aucun palier défini."));
    }
}
