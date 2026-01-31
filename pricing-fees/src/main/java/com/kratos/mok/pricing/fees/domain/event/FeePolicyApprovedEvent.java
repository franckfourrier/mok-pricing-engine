package com.kratos.mok.pricing.fees.domain.event;

import com.kratos.mok.pricing.shared.domain.event.PricingEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record FeePolicyApprovedEvent(
        String feePolicyId,
        String superAdminUser,
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
        return "FEES";
    }

    @Override
    public String action() {
        return "APPROVED";
    }

    @Override
    public String actor() {
        return superAdminUser;
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
