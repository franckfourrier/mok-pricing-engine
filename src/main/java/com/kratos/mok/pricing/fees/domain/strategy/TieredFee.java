package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.util.List;

public record TieredFee(List<Tier> tiers) implements FeeStrategy {
    @Override
    public Money apply(Money transactionAmount) {
        return tiers.stream()
                .filter(t -> t.covers(transactionAmount))
                .findFirst()
                .map(t -> t.calculate(transactionAmount))
                .orElseThrow(() -> new IllegalArgumentException("No tier found for this amount"));
    }
}
