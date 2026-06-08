package com.kratos.mok.pricing.taxes.infrastructure.repository;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxPolicyStatus;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import com.kratos.mok.pricing.taxes.domain.repository.TaxConfiguredTransactionCodeView;
import com.kratos.mok.pricing.taxes.infrastructure.model.TaxPolicyEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface JpaTaxPolicyRepository extends JpaRepository<TaxPolicyEntity, String>, JpaSpecificationExecutor<TaxPolicyEntity> {

    /*
     * Vérifie si un transactionCode est déjà utilisé dans le périmètre
     */
    @Query("""
        select count(distinct t) > 0
        from TaxPolicyEntity t
        join t.transactionCodes tc
        where tc in :transactionCodes
          and t.targetScope = :scope
          and t.targetValue = :value
          and t.status in ('DRAFT','PENDING_APPROVAL','ACTIVE','BLOCKED')
    """)
    boolean existsAnyForAnyTransactionCode(
            @Param("transactionCodes") Set<TransactionCode> transactionCodes,
            @Param("scope") TargetScope scope,
            @Param("value") String value
    );

    /*
     * Conflit lors update
     */
    @Query("""
        select count(distinct t) > 0
        from TaxPolicyEntity t
        join t.transactionCodes tc
        where tc in :transactionCodes
          and t.targetScope = :scope
          and t.targetValue = :value
          and t.strategyType = :strategyType
          and t.status in ('DRAFT','PENDING_APPROVAL','ACTIVE','BLOCKED')
          and (:excludedId is null or t.id <> :excludedId)
    """)
    boolean existsConflict(
            @Param("transactionCodes") Set<TransactionCode> transactionCodes,
            @Param("scope") TargetScope scope,
            @Param("value") String value,
            @Param("strategyType") TaxStrategyType strategyType,
            @Param("excludedId") String excludedId
    );

    /*
     * Taxes actives applicables à une transaction
     */
    @Query("""
        select distinct t
        from TaxPolicyEntity t
        join t.transactionCodes tc
        where tc = :transactionCode
          and t.status = :activeStatus
          and (
                (t.targetScope = 'GLOBAL' and upper(t.targetValue) = 'ALL')
             or (t.targetScope = 'ACCOUNT_TYPE' and :accountType is not null and t.targetValue = :accountType)
             or (t.targetScope = 'ACCOUNT_ID' and :accountId is not null and t.targetValue = :accountId)
          )
    """)
    List<TaxPolicyEntity> findActiveCandidates(
            @Param("transactionCode") TransactionCode transactionCode,
            @Param("accountType") String accountType,
            @Param("accountId") String accountId,
            @Param("activeStatus") TaxPolicyStatus activeStatus
    );

    /*
     * Liste des transactionCodes déjà configurés
     */
    /*@Query("""
        select distinct tc as transactionCode
        from TaxPolicyEntity e
        join e.transactionCodes tc
        where e.status in ('DRAFT','PENDING_APPROVAL','ACTIVE','SUSPENDED')
    """)*/
    @Query("""
    select tc as transactionCode,
           e.strategyType as strategyType
    from TaxPolicyEntity e
    join e.transactionCodes tc
    where e.status in ('DRAFT','PENDING_APPROVAL','ACTIVE','SUSPENDED')
    """)
    List<TaxConfiguredTransactionCodeView> findConfiguredTransactionCodes();
}