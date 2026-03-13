package com.kratos.mok.pricing.commissions.domain.event;

import java.time.LocalDateTime;

public record CommissionPlanUpdatedEvent(
        String commissionPlanId,
        String actor,
        String status,
        LocalDateTime occurredAt
) {}