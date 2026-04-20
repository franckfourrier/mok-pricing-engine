package com.kratos.mok.pricing.fees.domain.event;

import java.time.OffsetDateTime;

public record FeePolicyUpdatedEvent(
        String policyId,
        String actor,
        String status,
        OffsetDateTime occurredAt
) {}