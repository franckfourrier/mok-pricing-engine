package com.kratos.mok.pricing.fees.domain.event;

import com.kratos.mok.pricing.shared.domain.event.PricingEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record FeePolicyActivatedEvent(
        String feePolicyId,
        String adminUser,
        String justification,
        LocalDateTime when
) implements PricingEvent {

    @Override
    public String eventId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String aggregateId() {
        return feePolicyId;
    }

    @Override
    public String module() {
        return "FRAIS";
    }

    @Override
    public String action() {
        return "VALIDATION";
    }

    @Override
    public String actor() {
        return adminUser;
    }

    @Override
    public String reason() {
        return justification;
    }

    @Override
    public LocalDateTime occurredAt() {
        return when;
    }

    @Override
    public Map<String, Object> details() {
        return Map.of("status", "ACTIVE");
    }
}
