package com.kratos.mok.pricing.fees.infrastructure.repository;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaFeePolicyRepository extends JpaRepository<FeePolicyEntity, String>, JpaSpecificationExecutor<FeePolicyEntity> {

    /**
     * Retourne les policies candidates applicables pour une transaction à l'instant "at".
     * Les priorités sont déjà triées DESC (la meilleure est en tête).
     *
     * IMPORTANT: targetScope doit être cohérent avec l'enum TargetScope:
     * - GLOBAL
     * - ACCOUNT_TYPE
     * - ACCOUNT_ID
     */
    @Query("""
        SELECT f FROM FeePolicyEntity f
        WHERE f.transactionType = :type
          AND f.status = 'ACTIVE'
          AND (f.validityStart IS NULL OR f.validityStart <= :at)
          AND (f.validityEnd IS NULL OR f.validityEnd >= :at)
          AND (
              f.targetScope = 'GLOBAL'
              OR (f.targetScope = 'ACCOUNT_TYPE' AND f.targetValue = :accountType)
              OR (f.targetScope = 'ACCOUNT_ID' AND f.targetValue = :accountId)
          )
        ORDER BY f.priority DESC
    """)
    List<FeePolicyEntity> findActiveCandidates(
            @Param("type") TransactionType type,
            @Param("accountType") String accountType,
            @Param("accountId") String accountId,
            @Param("at") LocalDateTime at
    );

    /**
     * Utilisé par le bootstrap YAML : "existe-t-il déjà au moins une policy pour ce couple type/scope/value ?"
     */
    boolean existsByTransactionTypeAndTargetScopeAndTargetValue(
            TransactionType transactionType,
            TargetScope targetScope,
            String targetValue
    );

    /**
     * Détection de conflit "strict API" :
     * - même transactionType + targetScope + targetValue
     * - statut déjà présent dans le cycle de vie (ACTIVE/PENDING_APPROVAL/SUSPENDED)
     * - chevauchement de validité (en gérant les NULL = permanent/illimité)
     *
     * Condition de chevauchement:
     *   startA <= endB  AND  endA >= startB
     * avec NULL = -∞ pour start / +∞ pour end.
     */
    @Query("""
        SELECT COUNT(f) > 0 FROM FeePolicyEntity f
        WHERE f.transactionType = :type
          AND f.targetScope = :scope
          AND f.targetValue = :value
          AND f.status IN ('ACTIVE','PENDING_APPROVAL','SUSPENDED')
          AND (
               (f.validityStart IS NULL OR :end IS NULL OR f.validityStart <= :end)
           AND (f.validityEnd IS NULL OR :start IS NULL OR f.validityEnd >= :start)
          )
    """)
    boolean existsConflict(
            @Param("type") TransactionType type,
            @Param("scope") String scope,
            @Param("value") String value,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
