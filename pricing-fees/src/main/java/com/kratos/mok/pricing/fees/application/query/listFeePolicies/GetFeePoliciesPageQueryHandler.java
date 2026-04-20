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

        // Option simple: filtrage via Spring Data "Specification"
        var spec = FeePolicySpecifications.from(q);

        Page<FeePolicyEntity> entityPage = jpaRepository.findAll(spec, pageable);

        return PageResponseDto.from(entityPage.map(this::toSummary));
    }

    private FeePolicySummary toSummary(FeePolicyEntity e) {
        String shortId = e.getId().length() > 8
                ? e.getId().substring(0, 8).toUpperCase()
                : e.getId();

        return new FeePolicySummary(
                e.getId(),
                shortId,
                e.getTransactionType(),
                e.getTransactionCode().label(),
                e.getTransactionCode().sender(),
                e.getTransactionCode().receiver(),
                FeePolicyReadMapper.toTierSummaries(e.getStrategy()),
                e.getStatus(),
                e.getPriority(),
                e.getValidityStart(),
                e.getValidityEnd(),
                e.getCreatedBy().getTimestamp()
        );
    }
}

