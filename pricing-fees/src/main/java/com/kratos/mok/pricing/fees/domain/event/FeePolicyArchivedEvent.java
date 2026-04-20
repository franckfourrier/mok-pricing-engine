package com.kratos.mok.pricing.fees.domain.event;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record FeePolicyArchivedEvent(
        String policyId,
        String actor,
        String reason,
        OffsetDateTime occurredAt
) {}