package com.kratos.mok.pricing.commissions.domain.service;

import com.kratos.mok.pricing.commissions.domain.CommissionPlan;
import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CommissionPlanResolver {

    public CommissionPlan resolveBestPlan(List<CommissionPlan> candidates, LocalDateTime now) {
        if (candidates == null || candidates.isEmpty()) {
            throw new NotFoundException(
                    "COMMISSION_PLAN_NOT_FOUND",
                    "No commission plan configured for this transaction type / scope",
                    Map.of()
            );
        }

        var at = (now == null) ? LocalDateTime.now() : now;

        return candidates.stream()
                .filter(p -> p.isApplicableAt(at))
                .sorted(Comparator
                        .comparingInt((CommissionPlan p) -> p.priority().value()).reversed()
                        .thenComparing(p -> p.created().timestamp(), Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "COMMISSION_PLAN_NOT_APPLICABLE",
                        "No commission plan applicable at the provided time/context",
                        Map.of(
                                "at", at.toString()
                        )
                ));
    }
}
