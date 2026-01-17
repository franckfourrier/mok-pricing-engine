package com.kratos.mok.pricing.audit.domain;

import com.kratos.mok.pricing.audit.domain.enums.Priority;

import java.time.LocalDateTime;
import java.util.UUID;

public record Notification(
        String id,
        String recipient,
        String subject,
        String body,
        Priority priority,
        LocalDateTime createdAt
) {
    public static Notification create(String recipient, String subject, String body, Priority priority) {
        return new Notification(
                UUID.randomUUID().toString(),
                recipient, subject, body, priority, LocalDateTime.now()
        );
    }
}
