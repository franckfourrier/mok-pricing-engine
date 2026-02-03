package com.kratos.mok.pricing.taxes.domain.repository;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;

public interface TaxPolicyRepository {
    void save(TaxPolicy policy);

    boolean existsAnyFor(TransactionType type, TargetScope scope, String value);

    boolean existsConflictingPolicy(TaxPolicy policy);
}
