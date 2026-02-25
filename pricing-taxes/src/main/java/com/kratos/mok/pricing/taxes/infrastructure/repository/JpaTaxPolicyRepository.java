package com.kratos.mok.pricing.taxes.infrastructure.repository;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.taxes.domain.enums.TaxPolicyStatus;
import com.kratos.mok.pricing.taxes.infrastructure.model.TaxPolicyEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaTaxPolicyRepository extends JpaRepository<TaxPolicyEntity, String> {

    boolean existsByTransactionTypeAndTargetScopeAndTargetValue(
            TransactionType transactionType,
            TargetScope targetScope,
            String targetValue
    );

    @Query("""
        SELECT t FROM TaxPolicyEntity t
        WHERE t.transactionType = :type
          AND t.status = :activeStatus
          AND (
                (t.targetScope = 'GLOBAL' AND UPPER(t.targetValue) = 'ALL')
             OR (t.targetScope = 'ACCOUNT_TYPE' AND :accountType IS NOT NULL AND t.targetValue = :accountType)
             OR (t.targetScope = 'ACCOUNT_ID' AND :accountId IS NOT NULL AND t.targetValue = :accountId)
          )
    """)
    List<TaxPolicyEntity> findActiveCandidates(
            @Param("type") TransactionType type,
            @Param("accountType") String accountType,
            @Param("accountId") String accountId,
            @Param("activeStatus") TaxPolicyStatus activeStatus
    );

    @Query("""
        SELECT COUNT(t) > 0 FROM TaxPolicyEntity t
        WHERE t.transactionType = :type
          AND t.targetScope = :scope
          AND t.targetValue = :value
          AND t.status IN ('DRAFT','PENDING','ACTIVE','BLOCKED')
    """)
    boolean existsConflictV1(
            @Param("type") TransactionType type,
            @Param("scope") TargetScope scope,
            @Param("value") String value
    );

}
