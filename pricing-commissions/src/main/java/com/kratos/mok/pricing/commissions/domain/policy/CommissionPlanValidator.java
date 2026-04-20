package com.kratos.mok.pricing.commissions.domain.policy;

import com.kratos.mok.pricing.commissions.domain.enums.BeneficiaryType;
import com.kratos.mok.pricing.commissions.domain.strategy.*;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

public final class CommissionPlanValidator {

    private CommissionPlanValidator() {}

    private static final BigDecimal EPS = new BigDecimal("0.000001");

    public static void validate(CommissionStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("strategy is required");

        if (strategy instanceof SubscriberDepositStrategy s) {
            requireNonEmpty(s.keys(), "SUBSCRIBER_DEPOSIT keys");
            requireAllowedBeneficiaries(
                    s.keys(),
                    EnumSet.of(BeneficiaryType.AGENT, BeneficiaryType.DISTRIBUTOR, BeneficiaryType.SUPER_DISTRIBUTOR),
                    "SUBSCRIBER_DEPOSIT keys"
            );
            requireSumLessOrEqualOne(s.keys(), "SUBSCRIBER_DEPOSIT keys");

        } else if (strategy instanceof SubscriberWithdrawalStrategy s) {
            // agent + coverage <= 1
            /*BigDecimal agent = s.agentShare().value();
            BigDecimal cov = s.coverageRate().value();
            BigDecimal sum = agent.add(cov);
            if (sum.subtract(BigDecimal.ONE).compareTo(EPS) > 0) {
                throw new IllegalArgumentException("SUBSCRIBER_WITHDRAWAL requires (agent + coverage) <= 1. Got: " + sum);
            }
            // kratos >= 0 implicit
            if (s.kratosShare().value().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("SUBSCRIBER_WITHDRAWAL kratosShare must be >= 0");
            }*/

        } else if (strategy instanceof DirectStrategy s) {
            requireNonEmpty(s.keys(), "DIRECT keys");
            requireSumToOne(s.keys(), "DIRECT keys");

        } else {
            throw new IllegalArgumentException("Unknown strategy: " + strategy.getClass().getSimpleName());
        }
    }

    private static void requireNonEmpty(List<?> list, String label) {
        if (list == null || list.isEmpty()) throw new IllegalArgumentException(label + " must not be empty");
    }

    private static void requireAllowedBeneficiaries(List<CommissionShare> keys,
                                                    EnumSet<BeneficiaryType> allowed,
                                                    String label) {
        for (var k : keys) {
            if (!allowed.contains(k.beneficiaryType())) {
                throw new IllegalArgumentException(label + " has invalid beneficiary: " + k.beneficiaryType());
            }
        }
    }

    private static void requireSumToOne(List<CommissionShare> keys, String label) {
        BigDecimal sum = sum(keys);
        BigDecimal diff = sum.subtract(BigDecimal.ONE).abs();
        if (diff.compareTo(EPS) > 0) {
            throw new IllegalArgumentException(label + " must sum to 1.0 (100%). Got: " + sum);
        }
    }

    private static void requireSumLessOrEqualOne(List<CommissionShare> keys, String label) {
        BigDecimal sum = sum(keys);
        if (sum.subtract(BigDecimal.ONE).compareTo(EPS) > 0) {
            throw new IllegalArgumentException(label + " must sum <= 1.0 (100%). Got: " + sum);
        }
    }

    private static BigDecimal sum(List<CommissionShare> keys) {
        return keys.stream()
                .map(k -> k.share().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
