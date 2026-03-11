package com.kratos.mok.pricing.fees.domain.event;

import java.time.LocalDateTime;

public record FeePolicyUpdatedEvent(
        String policyId,
        String actor,
        String status,
        LocalDateTime occurredAt
) {}