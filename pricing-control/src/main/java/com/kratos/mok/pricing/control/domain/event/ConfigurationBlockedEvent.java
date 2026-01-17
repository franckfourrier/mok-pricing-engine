package com.kratos.mok.pricing.control.domain.event;

import com.kratos.mok.pricing.shared.domain.event.PricingEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Événement métier émis lorsqu'une configuration est BLOQUÉE
 * (conflit, non conformité réglementaire, règle critique).
 */
public record ConfigurationBlockedEvent(
        String aggregateId,
        String aggregateType,
        String actor,
        String blockCode,
        String reason,
        Map<String, Object> technicalDetails,
        LocalDateTime occurredAt
) implements PricingEvent {

    public ConfigurationBlockedEvent(
            String aggregateId,
            String aggregateType,
            String actor,
            String blockCode,
            String reason,
            Map<String, Object> technicalDetails
    ) {
        this(
                aggregateId,
                aggregateType,
                actor,
                blockCode,
                reason,
                technicalDetails,
                LocalDateTime.now()
        );
    }

    @Override
    public String eventId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String module() {
        return "CONTROL";
    }

    @Override
    public String action() {
        return "CONFIGURATION_BLOCKED";
    }

    @Override
    public Map<String, Object> details() {
        return technicalDetails;
    }
}
