package com.kratos.mok.pricing.taxes.application.command.rejectTaxPolicy;

import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;
import com.kratos.mok.pricing.taxes.domain.event.TaxPolicyApprovedEvent;
import com.kratos.mok.pricing.taxes.domain.repository.TaxPolicyRepository;
import com.kratos.mok.pricing.taxes.domain.vo.TaxPolicyId;
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
public class RejectTaxPolicyCommandHandler {

    private final TaxPolicyRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RejectTaxPolicyResponse handle(RejectTaxPolicyCommand cmd, String actor) {

        var policy = repository.findById(TaxPolicyId.from(cmd.policyId()))
                .orElseThrow(() -> new NotFoundException(
                        "TAX_POLICY_NOT_FOUND",
                        "TaxPolicy not found",
                        Map.of("id", cmd.policyId())
                ));

        String justification = (cmd.reason() == null || cmd.reason().isBlank())
                ? "APPROVE"
                : cmd.reason().trim();

        var now = LocalDateTime.now();

        policy.approve(actor, now, justification);

        repository.save(policy);

        eventPublisher.publishEvent(new TaxPolicyApprovedEvent(
                policy.id().value(),
                actor,
                justification,
                now
        ));

        return new RejectTaxPolicyResponse(policy.id().value(), true, policy.status().name());
    }
}

