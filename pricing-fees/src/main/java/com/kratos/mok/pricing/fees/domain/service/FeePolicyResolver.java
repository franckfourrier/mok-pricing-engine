package com.kratos.mok.pricing.fees.domain.service;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.TransactionContext;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class FeePolicyResolver {

    public FeePolicy resolveBestPolicy(List<FeePolicy> candidates, TransactionContext ctx, LocalDateTime now) {
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("No fee policy candidates found");
        }

        var at = (now == null) ? LocalDateTime.now() : now;

        return candidates.stream()
                .filter(p -> p.isApplicableAt(at, ctx))
                .sorted(Comparator
                        .comparingInt((FeePolicy p) -> p.priority().value()).reversed()
                        .thenComparing(p -> p.created().timestamp(), Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No applicable fee policy found"));
    }
}
