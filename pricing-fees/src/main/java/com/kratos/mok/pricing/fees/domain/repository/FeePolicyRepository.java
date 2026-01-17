package com.kratos.mok.pricing.fees.domain.repository;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;

import java.util.List;
import java.util.Optional;

public interface FeePolicyRepository {

    void save(FeePolicy policy);

    Optional<FeePolicy> findById(FeePolicyId id);

    List<FeePolicy> findCandidates(TransactionType type, String accountId, String profile);

    boolean existsConflictingPolicy(FeePolicy policy);
}
