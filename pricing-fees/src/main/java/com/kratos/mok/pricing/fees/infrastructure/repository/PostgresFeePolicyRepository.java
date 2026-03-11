package com.kratos.mok.pricing.fees.infrastructure.repository;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.fees.infrastructure.mapper.FeePolicyEntityMapper;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
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
        FeePolicyEntity entity = mapper.toEntity(policy);
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
        TargetScope scope = policy.target().scope();
        String value = normalize(scope, policy.target().value());

        LocalDateTime start = policy.validity().start();
        LocalDateTime end = policy.validity().end();

        LocalDateTime startBound = (start == null) ? LocalDateTime.of(1900, 1, 1, 0, 0) : start;
        LocalDateTime endBound = (end == null) ? LocalDateTime.of(9999, 12, 31, 23, 59, 59) : end;

        return jpaRepository.existsConflict(
                policy.transactionCode(),
                scope,
                value,
                startBound,
                endBound,
                policy.id().value()
        );
    }

    @Override
    public boolean existsAnyFor(TransactionCode transactionCode, TargetScope scope, String value) {
        return jpaRepository.existsByTransactionCodeAndTargetScopeAndTargetValue(
                transactionCode,
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
