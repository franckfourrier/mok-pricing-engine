package com.kratos.mok.pricing.commissions.domain.event;

import com.kratos.mok.pricing.shared.domain.event.PricingEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record CommissionPlanApprovedEvent(
        String commissionPlanId,
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
        return commissionPlanId;
    }

    @Override
    public String module() {
        return "COMMISSIONS";
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
