package com.kratos.mok.pricing.taxes.domain.event;

import com.kratos.mok.pricing.shared.domain.event.PricingEvent;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record TaxPolicyCreatedEvent(
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
        return "TAXES";
    }

    @Override
    public String action() {
        return "CREATED";
    }

    @Override
    public String reason() {
        return "Taxe policy created and submitted for approval";
    }

    @Override
    public Map<String, Object> details() {
        return Map.of();
    }
}
