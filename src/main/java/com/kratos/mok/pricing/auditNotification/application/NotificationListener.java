package com.kratos.mok.pricing.auditNotification.application;

import com.kratos.mok.pricing.auditNotification.domain.Notification;
import com.kratos.mok.pricing.auditNotification.domain.repository.NotificationSender;
import com.kratos.mok.pricing.shared.domain.event.PricingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationSender notificationSender;

    // TODO: Ces emails devraient idéalement venir d'une configuration externe (application.properties)
    private static final String EMAIL_ADMIN = "admin@kratos.com";
    private static final String EMAIL_SUPER_ADMIN = "super-admin@kratos.com";

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPricingEvent(PricingEvent event) {

        if ("BLOCKING".equals(event.action()) || "CONTROL".equals(event.module())) {
            sendCriticalAlert(event);
        }
        else if ("VALIDATION".equals(event.action())) {
            sendValidationInfo(event);
        }
    }

    private void sendCriticalAlert(PricingEvent event) {
        log.warn("[NOTIF] Envoi alerte critique pour blocage sur {}", event.aggregateId());

        var notif = Notification.create(
                EMAIL_ADMIN,
                "ALERTE CRITIQUE : " + event.module(),
                String.format("L'objet %s a été bloqué par %s.\nMotif : %s",
                        event.aggregateId(), event.actor(), event.reason()),
                Notification.Priority.CRITICAL
        );
        notificationSender.send(notif);
    }

    private void sendValidationInfo(PricingEvent event) {
        log.info("[NOTIF] Envoi info validation pour {}", event.aggregateId());

        var notif = Notification.create(
                EMAIL_SUPER_ADMIN,
                "Validation Effectuée : " + event.module(),
                String.format("La configuration %s a été validée avec succès par %s.",
                        event.aggregateId(), event.actor()),
                Notification.Priority.INFO
        );
        notificationSender.send(notif);
    }
}
