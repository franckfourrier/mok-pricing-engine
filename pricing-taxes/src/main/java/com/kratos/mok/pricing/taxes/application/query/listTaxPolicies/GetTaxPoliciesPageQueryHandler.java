package com.kratos.mok.pricing.taxes.application.query.listTaxPolicies;

import com.kratos.mok.pricing.shared.api.PageResponseDto;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxPolicyStatusMapper;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxPolicyUiMapper;
import com.kratos.mok.pricing.taxes.infrastructure.model.TaxPolicyEntity;
import com.kratos.mok.pricing.taxes.infrastructure.repository.JpaTaxPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTaxPoliciesPageQueryHandler {

    private final JpaTaxPolicyRepository jpaRepository;

    public PageResponseDto<TaxPolicySummary> handle(GetTaxPoliciesPageQuery q) {

        int page = Math.max(q.page(), 0);
        int size = Math.min(Math.max(q.size(), 1), 200);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        var spec = TaxPolicySpecifications.from(q);

        Page<TaxPolicyEntity> p = jpaRepository.findAll(spec, pageable);

       /*Page<TaxPolicySummary> mapped = p.map(e -> new TaxPolicySummary(
                e.getId(),
                null, // name
                e.getTransactionType(),
                e.getTargetScope(),
                e.getTargetValue(),
                e.getMode().name(),
                e.getStrategyType().name(),
                TaxPolicyReadMapper.value(e),
                TaxPolicyStatusMapper.toLabel(e.getStatus()),
                e.getCreatedAt()
        ));*/

         Page<TaxPolicySummary> mapped = p.map(e -> new TaxPolicySummary(
                 e.getId(),
                 TaxPolicyUiMapper.name(e),
                 TaxPolicyUiMapper.appliedTransaction(e.getTransactionType()),
                 TaxPolicyUiMapper.type(e),
                 TaxPolicyUiMapper.value(e),
                 e.getTransactionType(),
                 e.getTargetScope(),
                 e.getTargetValue(),
                 e.getStatus().name(),
                 TaxPolicyStatusMapper.toLabel(e.getStatus()),
                 TaxPolicyUiMapper.createdAt(e)
        ));


        return PageResponseDto.from(mapped);
    }
}