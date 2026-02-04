package com.kratos.mok.pricing.fees.application.command.approveFeePolicy;

import com.kratos.mok.pricing.fees.domain.event.FeePolicyApprovedEvent;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
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
public class ApproveFeePolicyCommandHandler {

    private final FeePolicyRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ApproveFeePolicyResponse handle(ApproveFeePolicyCommand cmd, String actor) {

        var policy = repository.findById(FeePolicyId.from(cmd.policyId()))
                .orElseThrow(() -> new NotFoundException(
                        "FEE_POLICY_NOT_FOUND",
                        "FeePolicy not found",
                        Map.of("id", cmd.policyId())
                ));

        String justification = (cmd.reason() == null || cmd.reason().isBlank())
                ? "APPROVE"
                : cmd.reason().trim();

        var now = LocalDateTime.now();

        policy.approve(actor, now, justification);

        repository.save(policy);

        eventPublisher.publishEvent(new FeePolicyApprovedEvent(
                policy.id().value(),
                actor,
                justification,
                now
        ));

        return new ApproveFeePolicyResponse(policy.id().value(), true, policy.status().name());
    }
}
