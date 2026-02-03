package com.kratos.mok.pricing.taxes.infrastructure.repository;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.repository.TaxPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostgresTaxPolicyRepository implements TaxPolicyRepository {

    private final JpaTaxPolicyRepository jpa;
    private final TaxPolicyEntityMapper mapper;

    @Override
    public void save(TaxPolicy policy) {
        jpa.save(mapper.fromDomain(policy));
    }

    @Override
    public boolean existsAnyFor(TransactionType type, TargetScope scope, String value) {
        return jpa.existsByTransactionTypeAndTargetScopeAndTargetValue(
                type,
                scope,
                normalize(scope, value)
        );
    }

    @Override
    public boolean existsConflictingPolicy(TaxPolicy policy) {
        if (policy == null) return false;

        TargetScope scope = policy.target().scope();
        String value = normalize(scope, policy.target().value());

        return jpa.existsConflictV1(
                policy.transactionType(),
                scope,
                value
        );
    }

    private String normalize(TargetScope scope, String value) {
        if (value == null) return null;
        String v = value.trim();
        return (scope == TargetScope.ACCOUNT_TYPE) ? v.toUpperCase() : v;
    }
}
