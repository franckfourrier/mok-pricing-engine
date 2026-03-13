package com.kratos.mok.pricing.taxes.infrastructure.repository;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.enums.TaxPolicyStatus;
import com.kratos.mok.pricing.taxes.domain.repository.TaxPolicyRepository;
import com.kratos.mok.pricing.taxes.domain.vo.TaxPolicyId;
import com.kratos.mok.pricing.taxes.infrastructure.mapper.TaxPolicyEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PostgresTaxPolicyRepository implements TaxPolicyRepository {

    private final JpaTaxPolicyRepository jpaRepository;
    private final TaxPolicyEntityMapper mapper;

    @Override
    public void save(TaxPolicy policy) {
        jpaRepository.save(mapper.fromDomain(policy));
    }

    @Override
    public Optional<TaxPolicy> findById(TaxPolicyId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsAnyForAnyTransactionCode(Set<TransactionCode> transactionCodes, TargetScope scope, String value) {
        return jpaRepository.existsAnyForAnyTransactionCode(
                transactionCodes,
                scope,
                normalize(scope, value)
        );
    }

    @Override
    public boolean existsConflictingPolicy(TaxPolicy policy) {
        if (policy == null) return false;

        TargetScope scope = policy.target().scope();
        String value = normalize(scope, policy.target().value());

        return jpaRepository.existsConflict(
                policy.transactionCodes(),
                scope,
                value,
                policy.id().value()
        );
    }

    @Override
    public List<TaxPolicy> findCandidates(String accountType, String accountId, TransactionCode transactionCode) {

        String normalizedAccountType = (accountType == null || accountType.isBlank())
                ? null
                : accountType.trim().toUpperCase();

        String normalizedAccountId = (accountId == null || accountId.isBlank())
                ? null
                : accountId.trim();

        return jpaRepository.findActiveCandidates(
                        transactionCode,
                        normalizedAccountType,
                        normalizedAccountId,
                        TaxPolicyStatus.ACTIVE
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    private String normalize(TargetScope scope, String value) {
        if (value == null) return null;
        String v = value.trim();
        return (scope == TargetScope.ACCOUNT_TYPE) ? v.toUpperCase() : v;
    }
}
