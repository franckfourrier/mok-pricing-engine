package com.kratos.mok.pricing.audit.application;

import com.kratos.mok.pricing.audit.domain.Notification;
import com.kratos.mok.pricing.audit.domain.NotificationPolicy;
import com.kratos.mok.pricing.audit.domain.enums.Priority;
import com.kratos.mok.pricing.audit.domain.repository.NotificationSender;
import com.kratos.mok.pricing.shared.domain.event.PricingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationSender notificationSender;

    // ⚠️ à externaliser plus tard
    private static final String EMAIL_ADMIN = "admin@kratos.com";
    private static final String EMAIL_SUPER_ADMIN = "super-admin@kratos.com";
    private static final String EMAIL_OPS = "ops@kratos.com";

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPricingEvent(PricingEvent event) {

        Priority priority = NotificationPolicy.resolve(event.action());

        String recipient = resolveRecipient(priority);
        String subject = buildSubject(event, priority);
        String body = buildBody(event);

        var notif = Notification.create(recipient, subject, body, priority);
        notificationSender.send(notif);

        log.info("[NOTIF] Sent priority={} action={} aggregateId={}",
                priority, event.action(), event.aggregateId());
    }

    private String resolveRecipient(Priority priority) {
        return switch (priority) {
            case CRITICAL -> EMAIL_SUPER_ADMIN; // ou admin + super admin
            case WARNING -> EMAIL_ADMIN;
            case INFO -> EMAIL_OPS;
        };
    }

    private String buildSubject(PricingEvent event, Priority priority) {
        return switch (priority) {
            case CRITICAL -> "ALERTE CRITIQUE: " + event.module() + " / " + event.action();
            case WARNING -> "Alerte: " + event.module() + " / " + event.action();
            case INFO -> "Info: " + event.module() + " / " + event.action();
        };
    }

    private String buildBody(PricingEvent event) {
        String details;
        try {
            Map<String, Object> d = event.details();
            details = (d == null || d.isEmpty()) ? "(no details)" : d.toString();
        } catch (Exception e) {
            details = "(details unavailable: " + e.getMessage() + ")";
        }

        return """
                Aggregate: %s
                Module: %s
                Action: %s
                Actor: %s
                Reason: %s
                OccurredAt: %s
                Details: %s
                """.formatted(
                event.aggregateId(),
                event.module(),
                event.action(),
                event.actor(),
                event.reason(),
                event.occurredAt(),
                details
        );
    }
}
