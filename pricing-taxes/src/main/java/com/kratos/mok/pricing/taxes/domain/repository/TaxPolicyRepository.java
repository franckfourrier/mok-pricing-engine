package com.kratos.mok.pricing.taxes.domain.repository;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.vo.TaxPolicyId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TaxPolicyRepository {
    void save(TaxPolicy policy);
    Optional<TaxPolicy> findById(TaxPolicyId id);

    boolean existsAnyForAnyTransactionCode(Set<TransactionCode> transactionCodes, TargetScope scope, String value);

    boolean existsConflictingPolicy(TaxPolicy policy);

    List<TaxPolicy> findCandidates(String accountType, String accountId, TransactionCode transactionCode);
}
