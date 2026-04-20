package com.kratos.mok.pricing.commissions.infrastructure.repository;

import com.kratos.mok.pricing.commissions.domain.CommissionPlan;
import com.kratos.mok.pricing.commissions.domain.repository.CommissionPlanRepository;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionPlanId;
import com.kratos.mok.pricing.commissions.infrastructure.mapper.CommissionPlanEntityMapper;
import com.kratos.mok.pricing.commissions.infrastructure.model.CommissionPlanEntity;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostgresCommissionPlanRepository implements CommissionPlanRepository {

    private final JpaCommissionPlanRepository jpaRepository;
    private final CommissionPlanEntityMapper mapper;
    private final TimeProvider timeProvider;

    @Override
    public void save(CommissionPlan plan) {
        CommissionPlanEntity entity = mapper.toEntity(plan);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<CommissionPlan> findById(CommissionPlanId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<CommissionPlan> findCandidates(TransactionType type, String accountType, String accountId) {
        var at = timeProvider.now();

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
    public List<CommissionPlan> findCandidates(TransactionCode transactionCode, String accountType, String accountId) {
         var at = timeProvider.now();

        String normalizedAccountType = (accountType == null || accountType.isBlank())
                ? null
                : accountType.trim().toUpperCase();

        String normalizedAccountId = (accountId == null || accountId.isBlank())
                ? null
                : accountId.trim();

        return jpaRepository.findActiveCandidatesByTransactionCode(
                        transactionCode,
                        normalizedAccountType,
                        normalizedAccountId,
                        at
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsConflictingPlan(CommissionPlan plan) {
        TargetScope scope = plan.target().scope();
        String value = normalize(scope, plan.target().value());

        OffsetDateTime start = plan.validity().start();
        OffsetDateTime end = plan.validity().end();

        OffsetDateTime startBound = (start == null)
                ? LocalDateTime.of(1900, 1, 1, 0, 0).atOffset(ZoneOffset.UTC)
                : start;

        OffsetDateTime endBound = (end == null)
                ? LocalDateTime.of(9999, 12, 31, 23, 59, 59).atOffset(ZoneOffset.UTC)
                : end;

        return jpaRepository.existsConflict(
                plan.transactionCode(),
                scope,
                value,
                startBound,
                endBound,
                plan.id().value()
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
