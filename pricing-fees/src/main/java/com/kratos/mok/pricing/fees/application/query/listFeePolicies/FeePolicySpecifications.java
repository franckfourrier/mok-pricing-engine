package com.kratos.mok.pricing.fees.application.query.listFeePolicies;

import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public final class FeePolicySpecifications {

    private FeePolicySpecifications() {}

    public static Specification<FeePolicyEntity> from(GetFeePoliciesPageQuery q) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (q.transactionCode() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("transactionType"), q.transactionCode()));
            }
            /*if (q.targetScope() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("targetScope"), q.targetScope()));
            }
            if (q.targetValue() != null && !q.targetValue().isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("targetValue"), q.targetValue().trim()));
            }*/
            if (q.status() != null && !q.status().isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), q.status().trim().toUpperCase()));
            }

            return predicates;
        };
    }
}
