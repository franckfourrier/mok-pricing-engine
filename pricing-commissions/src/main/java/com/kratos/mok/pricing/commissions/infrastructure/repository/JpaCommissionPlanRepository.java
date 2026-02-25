package com.kratos.mok.pricing.commissions.infrastructure.repository;

import com.kratos.mok.pricing.commissions.infrastructure.model.CommissionPlanEntity;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaCommissionPlanRepository
        extends JpaRepository<CommissionPlanEntity, String>, JpaSpecificationExecutor<CommissionPlanEntity> {

    /**
     * Retourne les plans candidats applicables pour une transaction à l'instant "at".
     * Les priorités sont déjà triées DESC (la meilleure est en tête).
     */
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

    boolean existsByTransactionTypeAndTargetScopeAndTargetValue(
            TransactionType transactionType,
            TargetScope targetScope,
            String targetValue
    );

    /**
     * Détection de conflit "strict API" :
     * - même transactionType + targetScope + targetValue
     * - statut déjà présent dans le cycle de vie
     * - chevauchement de validité (NULL = permanent/illimité)
     */
    @Query("""
        SELECT COUNT(c) > 0 FROM CommissionPlanEntity c
        WHERE c.transactionType = :type
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
    """)
    boolean existsConflict(
            @Param("type") TransactionType type,
            @Param("scope") TargetScope scope,
            @Param("value") String value,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
