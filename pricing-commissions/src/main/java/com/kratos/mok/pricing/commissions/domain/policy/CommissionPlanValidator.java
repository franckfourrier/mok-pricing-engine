package com.kratos.mok.pricing.commissions.domain.policy;

import com.kratos.mok.pricing.commissions.domain.strategy.*;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionKey;

import java.math.BigDecimal;

public final class CommissionPlanValidator {

    private CommissionPlanValidator() {}

    public static void validate(CommissionStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("strategy is required");

        if (strategy instanceof DepositDeferredStrategy s) {
            requireSumToOne(s.keys(), "DEPOSIT_DEFERRED keys");
        } else if (strategy instanceof DirectStrategy s) {
            requireSumToOne(s.keys(), "DIRECT keys");
        } else if (strategy instanceof WithdrawalCompensationStrategy s) {
            requireSumToOne(s.coverageKeys(), "WITHDRAWAL_COMPENSATION coverageKeys");
            requireSumToOne(s.surplusKeys(), "WITHDRAWAL_COMPENSATION surplusKeys");
            // coverageRate déjà borné 0..1 par Percentage
        } else {
            throw new IllegalArgumentException("Unknown strategy: " + strategy.getClass().getSimpleName());
        }
    }

    private static void requireSumToOne(java.util.List<CommissionKey> keys, String label) {
        BigDecimal sum = keys.stream()
                .map(k -> k.share().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // tolérance 0.000001 (6 décimales)
        BigDecimal diff = sum.subtract(BigDecimal.ONE).abs();
        if (diff.compareTo(new BigDecimal("0.000001")) > 0) {
            throw new IllegalArgumentException(label + " must sum to 1.0 (100%). Got: " + sum);
        }
    }
}
