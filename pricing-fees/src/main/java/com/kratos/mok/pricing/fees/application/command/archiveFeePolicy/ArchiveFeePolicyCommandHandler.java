package com.kratos.mok.pricing.fees.application.command.archiveFeePolicy;

import com.kratos.mok.pricing.fees.domain.event.FeePolicyArchivedEvent;
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
public class ArchiveFeePolicyCommandHandler {

    private final FeePolicyRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ArchiveFeePolicyResponse handle(ArchiveFeePolicyCommand cmd, String actor) {

        var policy = repository.findById(FeePolicyId.from(cmd.policyId()))
                .orElseThrow(() -> new NotFoundException(
                        "FEE_POLICY_NOT_FOUND",
                        "FeePolicy not found",
                        Map.of("id", cmd.policyId())
                ));

        var now = LocalDateTime.now();
        String reason = (cmd.reason() == null || cmd.reason().isBlank())
                ? "ARCHIVED"
                : cmd.reason().trim();

        policy.archive(actor, now, reason);

        repository.save(policy);

        eventPublisher.publishEvent(new FeePolicyArchivedEvent(
                policy.id().value(),
                actor,
                reason,
                now
        ));

        return new ArchiveFeePolicyResponse(
                policy.id().value(),
                true,
                policy.status().name()
        );
    }
}