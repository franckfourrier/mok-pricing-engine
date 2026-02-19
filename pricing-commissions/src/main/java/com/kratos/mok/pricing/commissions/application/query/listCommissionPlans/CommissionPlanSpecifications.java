package com.kratos.mok.pricing.commissions.application.query.listCommissionPlans;

import com.kratos.mok.pricing.commissions.infrastructure.model.CommissionPlanEntity;
import org.springframework.data.jpa.domain.Specification;

public final class CommissionPlanSpecifications {

    private CommissionPlanSpecifications() {}

    public static Specification<CommissionPlanEntity> from(GetCommissionPlansPageQuery q) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (q.transactionType() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("transactionType"), q.transactionType()));
            }
            if (q.targetScope() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("targetScope"), q.targetScope()));
            }
            if (q.targetValue() != null && !q.targetValue().isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("targetValue"), q.targetValue().trim()));
            }
            if (q.status() != null && !q.status().isBlank()) {
                // si status est Enum => adapte cb.equal(root.get("status"), Enum.valueOf(...))
                predicates = cb.and(predicates, cb.equal(root.get("status"), q.status().trim().toUpperCase()));
            }

            return predicates;
        };
    }
}
