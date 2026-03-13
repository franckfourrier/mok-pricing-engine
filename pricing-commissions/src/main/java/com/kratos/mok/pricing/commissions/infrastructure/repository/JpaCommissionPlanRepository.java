package com.kratos.mok.pricing.commissions.infrastructure.repository;

import com.kratos.mok.pricing.commissions.domain.repository.CommissionConfiguredTransactionCodeView;
import com.kratos.mok.pricing.commissions.infrastructure.model.CommissionPlanEntity;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaCommissionPlanRepository
        extends JpaRepository<CommissionPlanEntity, String>, JpaSpecificationExecutor<CommissionPlanEntity> {

    @Query("""
        SELECT c FROM CommissionPlanEntity c
        WHERE c.transactionType = :type
          AND c.status = com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus.ACTIVE
          AND (c.validityStart IS NULL OR c.validityStart <= :at)
          AND (c.validityEnd IS NULL OR c.validityEnd >= :at)
          AND (
              c.targetScope = com.kratos.mok.pricing.shared.domain.enums.TargetScope.GLOBAL
              OR (c.targetScope = com.kratos.mok.pricing.shared.domain.enums.TargetScope.ACCOUNT_TYPE AND c.targetValue = :accountType)
              OR (c.targetScope = com.kratos.mok.pricing.shared.domain.enums.TargetScope.ACCOUNT_ID AND c.targetValue = :accountId)
          )
        ORDER BY c.priority DESC
    """)
    List<CommissionPlanEntity> findActiveCandidates(
            @Param("type") TransactionType type,
            @Param("accountType") String accountType,
            @Param("accountId") String accountId,
            @Param("at") LocalDateTime at
    );

    @Query("""
        SELECT c FROM CommissionPlanEntity c
        WHERE c.transactionCode = :transactionCode
          AND c.status = com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus.ACTIVE
          AND (c.validityStart IS NULL OR c.validityStart <= :at)
          AND (c.validityEnd IS NULL OR c.validityEnd >= :at)
          AND (
              c.targetScope = com.kratos.mok.pricing.shared.domain.enums.TargetScope.GLOBAL
              OR (c.targetScope = com.kratos.mok.pricing.shared.domain.enums.TargetScope.ACCOUNT_TYPE AND c.targetValue = :accountType)
              OR (c.targetScope = com.kratos.mok.pricing.shared.domain.enums.TargetScope.ACCOUNT_ID AND c.targetValue = :accountId)
          )
        ORDER BY c.priority DESC
    """)
    List<CommissionPlanEntity> findActiveCandidatesByTransactionCode(
            @Param("transactionCode") TransactionCode transactionCode,
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
        SELECT COUNT(c) > 0 FROM CommissionPlanEntity c
        WHERE c.transactionCode = :transactionCode
          AND c.targetScope = :scope
          AND c.targetValue = :value
          AND c.status IN (
              com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus.ACTIVE,
              com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus.PENDING_APPROVAL,
              com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus.SUSPENDED
          )
          AND (
               (c.validityStart IS NULL OR c.validityStart <= :end)
           AND (c.validityEnd   IS NULL OR c.validityEnd   >= :start)
          )
          AND (:excludedId IS NULL OR c.id <> :excludedId)
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
        select distinct
            e.transactionCode as transactionCode
        from CommissionPlanEntity e
        where e.status in ('DRAFT', 'PENDING_APPROVAL', 'ACTIVE', 'SUSPENDED')
    """)
    List<CommissionConfiguredTransactionCodeView> findConfiguredTransactionCodes();
}