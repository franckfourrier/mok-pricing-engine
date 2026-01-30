package com.kratos.mok.pricing.fees.infrastructure.repository;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.fees.infrastructure.mapper.FeePolicyEntityMapper;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostgresFeePolicyRepository implements FeePolicyRepository {

    private final JpaFeePolicyRepository jpaRepository;
    private final FeePolicyEntityMapper mapper;

    @Override
    public void save(FeePolicy policy) {
        FeePolicyEntity entity = mapper.fromDomain(policy);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<FeePolicy> findById(FeePolicyId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<FeePolicy> findCandidates(TransactionType type, String accountType, String accountId) {
        LocalDateTime at = LocalDateTime.now();

        String normalizedAccountType = (accountType == null || accountType.isBlank())
                ? null
                : accountType.trim().toUpperCase();

        String normalizedAccountId = (accountId == null || accountId.isBlank())
                ? null
                : accountId.trim();

        return jpaRepository.findActiveCandidates(type, normalizedAccountType, normalizedAccountId, at)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsConflictingPolicy(FeePolicy policy) {
        String scope = policy.target().scope().name();
        String value = normalize(policy.target().scope(), policy.target().value());

        LocalDateTime start = policy.validity().start();
        LocalDateTime end = policy.validity().end();

        return jpaRepository.existsConflict(policy.transactionType(), scope, value, start, end);
    }

    @Override
    public boolean existsAnyFor(TransactionType transactionType, TargetScope scope, String value) {
        return jpaRepository.existsByTransactionTypeAndTargetScopeAndTargetValue(
                transactionType,
                scope,
                normalize(scope, value)
        );
    }

    private String normalize(TargetScope scope, String value) {
        if (value == null) return null;
        String v = value.trim();
        return (scope == TargetScope.ACCOUNT_TYPE) ? v.toUpperCase() : v;
    }
}
