package com.kratos.mok.pricing.control.domain.event;

import com.kratos.mok.pricing.shared.domain.event.PricingEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record ConfigurationBlockedEvent(
        String configId,
        String systemUser,
        String violationReason,
        Map<String, Object> technicalDetails
) implements PricingEvent {
    @Override public String eventId() { return UUID.randomUUID().toString(); }
    @Override public String aggregateId() { return configId; }
    @Override public String module() { return "CONTROL"; } // Déclenche la notif CRITIQUE
    @Override public String action() { return "BLOCKING"; }
    @Override public String actor() { return systemUser; }
    @Override public String reason() { return violationReason; } // ex: "Plafond BEAC dépassé"
    @Override public LocalDateTime occurredAt() { return LocalDateTime.now(); }
    @Override public Map<String, Object> details() { return technicalDetails; }
}
