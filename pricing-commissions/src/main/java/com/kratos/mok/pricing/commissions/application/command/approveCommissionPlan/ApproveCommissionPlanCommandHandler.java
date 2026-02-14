package com.kratos.mok.pricing.commissions.application.command.approveCommissionPlan;

import com.kratos.mok.pricing.commissions.domain.event.CommissionPlanApprovedEvent;
import com.kratos.mok.pricing.commissions.domain.repository.CommissionPlanRepository;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionPlanId;
import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApproveCommissionPlanCommandHandler {

    private final CommissionPlanRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ApproveCommissionPlanResponse handle(ApproveCommissionPlanCommand cmd, String actor) {

        var plan = repository.findById(CommissionPlanId.from(cmd.planId()))
                .orElseThrow(() -> new NotFoundException(
                        "COMMISSION_PLAN_NOT_FOUND",
                        "CommissionPlan not found",
                        Map.of("id", cmd.planId())
                ));

        String justification = (cmd.reason() == null || cmd.reason().isBlank())
                ? "APPROVE"
                : cmd.reason().trim();

        var now = LocalDateTime.now();

        plan.approve(actor, now, justification);

        repository.save(plan);

        eventPublisher.publishEvent(new CommissionPlanApprovedEvent(
                plan.id().value(),
                actor,
                justification,
                now
        ));

        return new ApproveCommissionPlanResponse(plan.id().value(), true, plan.status().name());
    }
}
