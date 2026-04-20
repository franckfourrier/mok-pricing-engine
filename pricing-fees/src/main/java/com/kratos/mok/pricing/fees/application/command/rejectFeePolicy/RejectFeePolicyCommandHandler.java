package com.kratos.mok.pricing.fees.application.command.rejectFeePolicy;

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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RejectFeePolicyCommandHandler {

    private final FeePolicyRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeProvider timeProvider;

    @Transactional
    public RejectFeePolicyResponse handle(RejectFeePolicyCommand cmd, String actor) {

        var policy = repository.findById(FeePolicyId.from(cmd.policyId()))
                .orElseThrow(() -> new NotFoundException(
                        "FEE_POLICY_NOT_FOUND",
                        "FeePolicy not found",
                        Map.of("id", cmd.policyId())
                ));

        String justification = (cmd.reason() == null || cmd.reason().isBlank())
                ? "REJECTED"
                : cmd.reason().trim();

        var now = timeProvider.now();

        policy.reject(actor, now, justification);

        repository.save(policy);

        eventPublisher.publishEvent(new FeePolicyApprovedEvent(
                policy.id().value(),
                actor,
                justification,
                now
        ));

        return new RejectFeePolicyResponse(policy.id().value(), true, policy.status().name());
    }
}
