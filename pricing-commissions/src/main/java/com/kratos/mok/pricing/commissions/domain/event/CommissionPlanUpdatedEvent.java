package com.kratos.mok.pricing.commissions.domain.event;

import java.time.OffsetDateTime;

public record CommissionPlanUpdatedEvent(
        String commissionPlanId,
        String actor,
        String status,
        OffsetDateTime occurredAt
) {}