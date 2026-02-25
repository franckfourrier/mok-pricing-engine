package com.kratos.mok.pricing.commissions.application.query.listCommissionPlans;

import com.kratos.mok.pricing.commissions.infrastructure.model.CommissionPlanEntity;
import com.kratos.mok.pricing.commissions.infrastructure.repository.JpaCommissionPlanRepository;
import com.kratos.mok.pricing.shared.api.PageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCommissionPlansPageQueryHandler {

    private final JpaCommissionPlanRepository jpaRepository;

    public PageResponseDto<CommissionPlanSummary> handle(GetCommissionPlansPageQuery q) {
        int page = Math.max(q.page(), 0);
        int size = Math.min(Math.max(q.size(), 1), 200);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdBy.timestamp"));
        var spec = CommissionPlanSpecifications.from(q);

        Page<CommissionPlanEntity> p = jpaRepository.findAll(spec, pageable);
        Page<CommissionPlanSummary> mapped = p.map(CommissionPlanReadMapper::toSummary);

        return PageResponseDto.from(mapped);
    }
}