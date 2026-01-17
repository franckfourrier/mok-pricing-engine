package com.kratos.mok.pricing.fees.domain.service;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FeePolicyResolver {

    public Optional<FeePolicy> resolveBestPolicy(List<FeePolicy> candidates) {
        // On trie par priorité décroissante (2 > 1 > 0) et on prend la première
        return candidates.stream()
                .max(Comparator.comparingInt(FeePolicy::priority));
    }
}