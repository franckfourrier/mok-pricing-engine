package com.kratos.mok.pricing.fees.infrastructure.repository;

import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaFeePolicyRepository extends JpaRepository<FeePolicyEntity, String> {

    @Query("""
        SELECT f FROM FeePolicyEntity f
        WHERE f.transactionType = :type
          AND f.status = 'ACTIVE'
          AND (f.validityStart IS NULL OR f.validityStart <= CURRENT_TIMESTAMP)
          AND (f.validityEnd IS NULL OR f.validityEnd >= CURRENT_TIMESTAMP)
          AND (
              f.targetScope = 'GLOBAL'
              OR (f.targetScope = 'PROFILE' AND f.targetValue = :profile)
              OR (f.targetScope = 'INDIVIDUAL' AND f.targetValue = :accountId)
          )
        ORDER BY f.priority DESC
    """)
    List<FeePolicyEntity> findActiveCandidates(
            @Param("type") TransactionType type,
            @Param("accountId") String accountId,
            @Param("profile") String profile
    );
}

