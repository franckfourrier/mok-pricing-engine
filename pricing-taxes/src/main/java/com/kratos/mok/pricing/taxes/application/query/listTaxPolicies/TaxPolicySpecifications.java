package com.kratos.mok.pricing.taxes.application.query.listTaxPolicies;

import com.kratos.mok.pricing.taxes.infrastructure.model.TaxPolicyEntity;
import org.springframework.data.jpa.domain.Specification;

public final class TaxPolicySpecifications {

    private TaxPolicySpecifications() {}

    public static Specification<TaxPolicyEntity> from(GetTaxPoliciesPageQuery q) {
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
                predicates = cb.and(predicates, cb.equal(root.get("status"), q.status().trim().toUpperCase()));
            }

            return predicates;
        };
    }
}