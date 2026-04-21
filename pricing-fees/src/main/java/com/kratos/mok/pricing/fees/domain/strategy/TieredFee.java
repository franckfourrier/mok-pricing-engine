package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public record TieredFee(List<Tier> tiers) implements FeeStrategy {

    public TieredFee {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("Une stratégie par paliers doit contenir au moins un palier.");
        }

        var sorted = tiers.stream()
                .sorted(Comparator.comparing(Tier::min))
                .toList();

        validateTiersCoherence(sorted);

        tiers = List.copyOf(sorted);
    }

    @Override
    public FeeStrategyType type() {
        return FeeStrategyType.TIERED;
    }

    private static void validateTiersCoherence(List<Tier> tiers) {

        List<Tier> sortedTiers = tiers.stream()
                .sorted(Comparator.comparing(t -> normalize(t.min())))
                .toList();

        Tier previous = null;

        for (Tier current : sortedTiers) {

            long currMin = normalize(current.min());
            long currMax = normalize(current.max());

            if (currMin >= currMax) {
                throw new IllegalArgumentException(
                        "Palier invalide : [" + currMin + " - " + currMax + "]"
                );
            }

            if (previous != null) {

                long prevMax = normalize(previous.max());
                long expectedMin = prevMax + 1;

                if (currMin != expectedMin) {
                    throw new IllegalArgumentException(
                            "Continuité rompue : attendu " + expectedMin +
                                    " après " + prevMax + " mais trouvé " + currMin
                    );
                }
            }

            previous = current;
        }
    }

    private static long normalize(Money money) {
        if (money == null || money.amount() == null) {
            throw new IllegalArgumentException("Money is required for tier validation");
        }

        return money.amount()
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
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
