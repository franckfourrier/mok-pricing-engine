package com.kratos.mok.pricing.taxes.application.query.listTaxPolicies;

import com.kratos.mok.pricing.shared.api.PageResponseDto;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxPolicyStatusMapper;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxPolicyUiMapper;
import com.kratos.mok.pricing.taxes.infrastructure.model.TaxPolicyEntity;
import com.kratos.mok.pricing.taxes.infrastructure.repository.JpaTaxPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

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

        Page<TaxPolicySummary> mapped = p.map(this::toSummary);

        return PageResponseDto.from(mapped);
    }

    private TaxPolicySummary toSummary(TaxPolicyEntity e) {
        List<String> appliedTransactions = e.getTransactionCodes().stream()
                .sorted(Comparator.comparing(Enum::name))
                .map(TransactionCode::label)
                .toList();

        return new TaxPolicySummary(
                e.getId(),
                TaxPolicyUiMapper.name(e),
                appliedTransactions,
                TaxPolicyUiMapper.type(e),
                TaxPolicyUiMapper.value(e),
                e.getTargetScope(),
                e.getTargetValue(),
                e.getStatus().name(),
                TaxPolicyStatusMapper.toLabel(e.getStatus()),
                TaxPolicyUiMapper.createdAt(e)
        );
    }
}
