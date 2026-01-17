package com.kratos.mok.pricing.fees.infrastructure.repository;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.fees.infrastructure.mapper.FeePolicyMapper;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostgresFeePolicyRepository implements FeePolicyRepository {

    private final JpaFeePolicyRepository jpaRepository;
    private final FeePolicyMapper mapper;

    @Override
    public void save(FeePolicy policy) {
        FeePolicyEntity entity = mapper.toEntity(policy);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<FeePolicy> findById(FeePolicyId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<FeePolicy> findCandidates(TransactionType type, String accountId, String profile) {
        List<FeePolicyEntity> entities = jpaRepository.findActiveCandidates(type, accountId, profile);
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }
}