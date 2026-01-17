package com.kratos.mok.pricing.shared.domain.event;

import java.time.LocalDateTime;
import java.util.Map;

public interface PricingEvent {
    String eventId();
    String aggregateId();
    String actor();
    LocalDateTime occurredAt();
    String module();
    String action();
    String reason();
    Map<String, Object> details();
}
