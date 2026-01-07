package com.kratos.mok.pricing.fees.infrastructure.repository;

import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaFeePolicyRepository extends JpaRepository<FeePolicyEntity, UUID> {

    @Query("""
        SELECT f FROM FeePolicyEntity f 
        WHERE f.transactionType = :type 
        AND f.status = 'ACTIVE' 
        AND (
            f.targetScope = 'GLOBAL' 
            OR (f.targetScope = 'PROFILE' AND f.targetValue = :profile)
            OR (f.targetScope = 'INDIVIDUAL' AND f.targetValue = :accountId)
        )
    """)
    List<FeePolicyEntity> findActiveCandidates(
            @Param("type") TransactionType type,
            @Param("accountId") String accountId,
            @Param("profile") String profile
    );
}
