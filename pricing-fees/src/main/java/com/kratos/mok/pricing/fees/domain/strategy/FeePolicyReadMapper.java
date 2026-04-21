package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.fees.application.query.listFeePolicies.FeeTierSummary;
import com.kratos.mok.pricing.shared.api.MoneyDto;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.math.BigDecimal;
import java.util.List;

public final class FeePolicyReadMapper {

    private static final String DEFAULT_CURRENCY = "XAF";

    private FeePolicyReadMapper() {}

    public static List<FeeTierSummary> toTierSummaries(FeeStrategy strategy) {
        if (strategy == null) return List.of();

        if (strategy instanceof TieredFee tiered) {
            return tiered.tiers().stream()
                    .map(FeePolicyReadMapper::mapTier)
                    .toList();
        }

        // stratégie “plate” => 1 tier normalisé
        return List.of(mapFlat(strategy));
    }

    private static FeeTierSummary mapTier(Tier t) {
        var min = toMoneyDto(t.min());
        var max = toMoneyDto(t.max());

        FeeStrategy s = t.strategy();

        if (s instanceof FixedFee ff) {
            return new FeeTierSummary(
                    min, max,
                    "FIXED",
                    formatMoney(ff.feeAmount()),
                    null
            );
        }

        if (s instanceof ProportionalFee pf) {
            return new FeeTierSummary(
                    min, max,
                    "PROPORTIONAL",
                    null,
                    formatPercentage(pf.percentage().value())
            );
        }

        throw new IllegalStateException("Unsupported tier strategy: " + s.getClass().getSimpleName());
    }

    private static FeeTierSummary mapFlat(FeeStrategy s) {
        var min = new MoneyDto(BigDecimal.ZERO.setScale(2), DEFAULT_CURRENCY);
        var max = new MoneyDto(new BigDecimal("9999999999.99"), DEFAULT_CURRENCY);

        if (s instanceof FixedFee ff) {
            return new FeeTierSummary(
                    min, max,
                    "FIXED",
                    formatMoney(ff.feeAmount()),
                    null
            );
        }

        if (s instanceof ProportionalFee pf) {
            return new FeeTierSummary(
                    min, max,
                    "PROPORTIONAL",
                    null,
                    formatPercentage(pf.percentage().value())
            );
        }

        throw new IllegalStateException("Unsupported strategy: " + s.getClass().getSimpleName());
    }

    private static MoneyDto toMoneyDto(com.kratos.mok.pricing.shared.domain.vo.Money m) {
        return new MoneyDto(m.amount(), m.currency());
    }

    private static String formatMoney(Money m) {
        if (m == null) return "0 XAF";
        // Retourne "50 XAF" au lieu de {"amount": 50, "currency": "XAF"}
        return m.amount().stripTrailingZeros().toPlainString() + " " + m.currency();
    }

    private static String formatPercentage(BigDecimal value) {
        if (value == null) return "0%";
        return value.stripTrailingZeros().toPlainString() + "%";
    }
}
