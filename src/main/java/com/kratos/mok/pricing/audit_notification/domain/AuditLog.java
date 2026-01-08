package com.kratos.mok.pricing.audit_notification.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLog(
        String id,
        String module,
        String action,
        String aggregateId,
        String actor,
        String reason,
        String payloadJson,
        LocalDateTime timestamp
) {
    public static AuditLog create(String module, String action, String aggId, String actor, String reason, String payload) {
        return new AuditLog(UUID.randomUUID().toString(), module, action, aggId, actor, reason, payload, LocalDateTime.now());
    }
}
