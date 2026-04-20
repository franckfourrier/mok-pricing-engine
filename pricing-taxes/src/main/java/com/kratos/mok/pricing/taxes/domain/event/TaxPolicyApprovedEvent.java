package com.kratos.mok.pricing.taxes.domain.event;

import com.kratos.mok.pricing.shared.domain.event.PricingEvent;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record TaxPolicyApprovedEvent(
        String taxePolicyId,
        String superAdminUser,
        String justification,
        OffsetDateTime when
) implements PricingEvent {

    @Override
    public String eventId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String aggregateId() {
        return taxePolicyId;
    }

    @Override
    public String module() {
        return "TAXES";
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
    public OffsetDateTime occurredAt() {
        return when;
    }

    @Override
    public Map<String, Object> details() {
        return Map.of("status", "ACTIVE");
    }
}
