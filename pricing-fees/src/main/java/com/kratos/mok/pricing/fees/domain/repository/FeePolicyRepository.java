package com.kratos.mok.pricing.fees.domain.repository;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;

import java.util.List;
import java.util.Optional;

public interface FeePolicyRepository {

    void save(FeePolicy policy);

    Optional<FeePolicy> findById(FeePolicyId id);

    List<FeePolicy> findCandidates(TransactionType type, String accountType, String accountId);

    List<FeePolicy> findCandidates(TransactionCode transactionCode, String accountType, String accountId);

    boolean existsConflictingPolicy(FeePolicy policy);

    boolean existsAnyFor(TransactionCode transactionCode, TargetScope scope, String value);
}