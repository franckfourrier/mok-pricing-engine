package com.kratos.mok.pricing.fees.application.query.listFeePolicies;

import com.kratos.mok.pricing.fees.domain.strategy.FeePolicyReadMapper;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import com.kratos.mok.pricing.fees.infrastructure.repository.JpaFeePolicyRepository;
import com.kratos.mok.pricing.shared.api.PageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetFeePoliciesPageQueryHandler {

    private final JpaFeePolicyRepository jpaRepository;

    public PageResponseDto<FeePolicySummary> handle(GetFeePoliciesPageQuery q) {

        int page = Math.max(q.page(), 0);
        int size = Math.min(Math.max(q.size(), 1), 200);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdBy.timestamp"));

        // Option simple: filtrage via Spring Data "Specification" (recommandé)
        var spec = FeePolicySpecifications.from(q);

        Page<FeePolicyEntity> p = jpaRepository.findAll(spec, pageable);

        Page<FeePolicySummary> mapped = p.map(e -> new FeePolicySummary(
                e.getId(),
                e.getTransactionType(),
                e.getTargetScope(),
                e.getTargetValue(),
                FeePolicyReadMapper.toTierSummaries(e.getStrategy()),
                e.getStatus(),
                e.getPriority(),
                e.getValidityStart(),
                e.getValidityEnd(),
                e.getCreatedBy().getTimestamp()
        ));

        return PageResponseDto.from(mapped);
    }
}

