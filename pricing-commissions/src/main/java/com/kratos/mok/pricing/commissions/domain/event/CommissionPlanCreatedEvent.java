package com.kratos.mok.pricing.commissions.domain.event;

import com.kratos.mok.pricing.shared.domain.event.PricingEvent;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record CommissionPlanCreatedEvent(
        String aggregateId,
        String actor,
        OffsetDateTime occurredAt
) implements PricingEvent {

    @Override
    public String eventId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String module() {
        return "COMMISSIONS";
    }

    @Override
    public String action() {
        return "CREATED";
    }

    @Override
    public String reason() {
        return "Commission plan created and submitted for approval";
    }

    @Override
    public Map<String, Object> details() {
        return Map.of();
    }
}
