package com.kratos.mok.pricing.fees.infrastructure.repository;

import com.kratos.mok.pricing.fees.domain.repository.FeePolicyConfiguredOptionView;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaFeePolicyRepository extends JpaRepository<FeePolicyEntity, String>, JpaSpecificationExecutor<FeePolicyEntity> {

    @Query("""
        SELECT f FROM FeePolicyEntity f
        WHERE f.transactionType = :type
          AND f.status = 'ACTIVE'
          AND (f.validityStart IS NULL OR f.validityStart <= :at)
          AND (f.validityEnd IS NULL OR f.validityEnd >= :at)
          AND (
              f.targetScope = com.kratos.mok.pricing.shared.domain.enums.TargetScope.GLOBAL
              OR (f.targetScope = com.kratos.mok.pricing.shared.domain.enums.TargetScope.ACCOUNT_TYPE AND f.targetValue = :accountType)
              OR (f.targetScope = com.kratos.mok.pricing.shared.domain.enums.TargetScope.ACCOUNT_ID AND f.targetValue = :accountId)
          )
        ORDER BY f.priority DESC
    """)
    List<FeePolicyEntity> findActiveCandidates(
            @Param("type") TransactionType type,
            @Param("accountType") String accountType,
            @Param("accountId") String accountId,
            @Param("at") LocalDateTime at
    );

    boolean existsByTransactionCodeAndTargetScopeAndTargetValue(
            TransactionCode transactionCode,
            TargetScope targetScope,
            String targetValue
    );

    @Query("""
        SELECT COUNT(f) > 0 FROM FeePolicyEntity f
        WHERE f.transactionCode = :transactionCode
          AND f.targetScope = :scope
          AND f.targetValue = :value
          AND f.status IN ('ACTIVE','PENDING_APPROVAL','SUSPENDED')
          AND (
               (f.validityStart IS NULL OR f.validityStart <= :end)
           AND (f.validityEnd   IS NULL OR f.validityEnd   >= :start)
          )
          AND (:excludedId IS NULL OR f.id <> :excludedId)
    """)
    boolean existsConflict(
            @Param("transactionCode") TransactionCode transactionCode,
            @Param("scope") TargetScope scope,
            @Param("value") String value,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludedId") String excludedId
    );

    @Query("""
        select
            e.transactionCode as transactionCode,
            e.targetScope as targetScope,
            e.targetValue as targetValue
        from FeePolicyEntity e
        where e.status <> 'ARCHIVED'
    """)
    List<FeePolicyConfiguredOptionView> findConfiguredOptions();
}