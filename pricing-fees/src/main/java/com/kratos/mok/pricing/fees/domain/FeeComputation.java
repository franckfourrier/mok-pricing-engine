// src/main/java/com/kratos/mok/pricing/fees/domain/FeeComputation.java
package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.shared.domain.vo.Money;

/**
 * Résultat détaillé d'un calcul de frais.
 * Sert pour audit, notification, debug et traçabilité.
 */
public record FeeComputation(
        FeePolicyId policyId,
        Money transactionAmount,
        Money feeAmount,
        FeeStrategyType strategyType,
        String ruleApplied,
        Money activationThreshold,
        Money minLimit,
        Money maxLimit
) {
    public static FeeComputation free(
            FeePolicyId policyId,
            Money transactionAmount,
            FeeStrategyType strategyType,
            String ruleApplied,
            Money activationThreshold,
            Money minLimit,
            Money maxLimit
    ) {
        return new FeeComputation(
                policyId,
                transactionAmount,
                Money.ZERO,
                strategyType,
                ruleApplied,
                activationThreshold,
                minLimit,
                maxLimit
        );
    }
}
