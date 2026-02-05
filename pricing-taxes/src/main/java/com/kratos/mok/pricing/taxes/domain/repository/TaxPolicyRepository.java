package com.kratos.mok.pricing.taxes.domain.repository;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.vo.TaxPolicyId;

import java.util.List;
import java.util.Optional;

public interface TaxPolicyRepository {
    void save(TaxPolicy policy);
    Optional<TaxPolicy> findById(TaxPolicyId id);
    boolean existsAnyFor(TransactionType type, TargetScope scope, String value);

    List<TaxPolicy> findCandidates(TransactionType type, String accountType, String accountId);

    boolean existsConflictingPolicy(TaxPolicy policy);
}
