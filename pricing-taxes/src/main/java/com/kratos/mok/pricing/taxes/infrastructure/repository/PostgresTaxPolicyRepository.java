package com.kratos.mok.pricing.taxes.infrastructure.repository;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.repository.TaxPolicyRepository;
import com.kratos.mok.pricing.taxes.domain.vo.TaxPolicyId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostgresTaxPolicyRepository implements TaxPolicyRepository {

    private final JpaTaxPolicyRepository jpaRepository;
    private final TaxPolicyEntityMapper mapper;

    @Override
    public void save(TaxPolicy policy) {
        jpaRepository.save(mapper.fromDomain(policy));
    }

    @Override
    public Optional<TaxPolicy> findById(TaxPolicyId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsAnyFor(TransactionType type, TargetScope scope, String value) {
        return jpaRepository.existsByTransactionTypeAndTargetScopeAndTargetValue(
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

        return jpaRepository.existsConflictV1(
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
