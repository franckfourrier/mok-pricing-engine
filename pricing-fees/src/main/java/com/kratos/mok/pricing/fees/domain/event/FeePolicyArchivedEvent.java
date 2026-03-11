package com.kratos.mok.pricing.fees.domain.event;

import java.time.LocalDateTime;

public record FeePolicyArchivedEvent(
        String policyId,
        String actor,
        String reason,
        LocalDateTime occurredAt
) {}