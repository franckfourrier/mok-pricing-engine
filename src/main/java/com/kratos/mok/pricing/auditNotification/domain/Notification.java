package com.kratos.mok.pricing.auditNotification.domain;

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
    public enum Priority { INFO, WARNING, CRITICAL }

    public static Notification create(String recipient, String subject, String body, Priority priority) {
        return new Notification(
                UUID.randomUUID().toString(),
                recipient, subject, body, priority, LocalDateTime.now()
        );
    }
}
