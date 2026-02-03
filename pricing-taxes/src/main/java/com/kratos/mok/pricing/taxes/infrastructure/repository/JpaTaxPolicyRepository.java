package com.kratos.mok.pricing.taxes.infrastructure.repository;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.taxes.infrastructure.model.TaxPolicyEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface JpaTaxPolicyRepository extends JpaRepository<TaxPolicyEntity, String> {

    boolean existsByTransactionTypeAndTargetScopeAndTargetValue(
            TransactionType transactionType,
            TargetScope targetScope,
            String targetValue
    );

    @Query("""
        SELECT COUNT(t) > 0 FROM TaxPolicyEntity t
        WHERE t.transactionType = :type
          AND t.targetScope = :scope
          AND t.targetValue = :value
          AND t.status IN ('DRAFT','PENDING_APPROVAL','ACTIVE','BLOCKED')
    """)
    boolean existsConflictV1(
            @Param("type") TransactionType type,
            @Param("scope") TargetScope scope,
            @Param("value") String value
    );
}
