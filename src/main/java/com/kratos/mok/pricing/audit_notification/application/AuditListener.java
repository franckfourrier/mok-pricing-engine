package com.kratos.mok.pricing.audit_notification.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kratos.mok.pricing.audit_notification.domain.AuditLog;
import com.kratos.mok.pricing.audit_notification.domain.repository.AuditRepository;
import com.kratos.mok.pricing.shared.domain.event.PricingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuditListener {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPricingEvent(PricingEvent event) {
        try {
            // Conversion des détails techniques en JSON pour archivage
            String payload = objectMapper.writeValueAsString(event.details());

            AuditLog logEntry = AuditLog.create(
                    event.module(),
                    event.action(),
                    event.aggregateId(),
                    event.actor(),
                    event.reason(),
                    payload
            );

            auditRepository.save(logEntry);
            log.info("[AUDIT] Enregistré - Module:{} Action:{} ID:{}", event.module(), event.action(), event.aggregateId());

        } catch (JsonProcessingException e) {
            log.error("[AUDIT] Erreur critique de sérialisation JSON pour l'événement {}", event.eventId(), e);
            // Ici, on pourrait sauvegarder un log "dégradé" sans payload pour ne pas perdre la trace
        }
    }
}