package com.kratos.mok.pricing.fees.domain.service;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.TransactionContext;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FeePolicyResolver {

    /**
     * Résout la meilleure policy applicable parmi les candidates déjà récupérées.
     * Règle:
     * 1) garder seulement celles applicables à (now, ctx)
     * 2) priorité DESC
     * 3) tie-breaker: createdAt DESC (optionnel mais utile)
     */
    public Optional<FeePolicy> resolveBestPolicy(List<FeePolicy> candidates, TransactionContext ctx, LocalDateTime now) {
        if (candidates == null || candidates.isEmpty()) return Optional.empty();

        var at = (now == null) ? LocalDateTime.now() : now;

        return candidates.stream()
                .filter(p -> p.isApplicableAt(at, ctx))
                .sorted(Comparator
                        .comparingInt((FeePolicy p) -> p.priority().value()).reversed()
                        .thenComparing(p -> p.created().timestamp(), Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .findFirst();
    }
}
