package com.kratos.mok.pricing.fees.application.command.approveFeePolicy;

import com.kratos.mok.pricing.fees.domain.event.FeePolicyApprovedEvent;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
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
    private final TimeProvider timeProvider;

    @Transactional
    public ApproveFeePolicyResponse handle(ApproveFeePolicyCommand cmd, String actor) {

        var policy = repository.findById(FeePolicyId.from(cmd.policyId()))
                .orElseThrow(() -> new NotFoundException(
                        "FEE_POLICY_NOT_FOUND",
                        "FeePolicy not found",
                        Map.of("id", cmd.policyId())
                ));

        var now = timeProvider.now();

        policy.approve(actor, now);

        repository.save(policy);

        eventPublisher.publishEvent(new FeePolicyApprovedEvent(
                policy.id().value(),
                actor,
                "APPROVED",
                now
        ));

        return new ApproveFeePolicyResponse(policy.id().value(), true, policy.status().name());
    }
}
