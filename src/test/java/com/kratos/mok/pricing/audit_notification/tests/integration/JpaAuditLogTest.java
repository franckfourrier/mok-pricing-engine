package com.kratos.mok.pricing.audit_notification.tests.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kratos.mok.pricing.audit_notification.infrastructure.entity.AuditLogEntity;
import com.kratos.mok.pricing.audit_notification.infrastructure.repository.JpaAuditRepository;
import com.kratos.mok.pricing.shared.domain.event.PricingEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureJson
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class JpaAuditLogTest {
    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JpaAuditRepository jpaRepository;

    @Test
    @DisplayName("LOCAL DB: L'événement doit être écrit dans votre PostgreSQL local")
    void should_write_to_local_postgres() {
        // GIVEN
        String aggId = "POLICY-" + System.currentTimeMillis();
        var event = new LocalTestEvent(aggId);

        // WHEN
        transactionTemplate.executeWithoutResult(status -> {
            publisher.publishEvent(event);
        });

        // THEN
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            var logOptional = jpaRepository.findByAggregateId(aggId);
            assertThat(logOptional).isPresent();
            var log = logOptional.get();

            assertThat(log.getModule()).isEqualTo("TEST_LOCAL");
            assertThat(log.getAction()).isEqualTo("VERIFICATION");
            assertThat(log.getActor()).isEqualTo("moi");
            // Vérifie que le JSONB a bien été stocké par Postgres
            assertThat(log.getPayload()).contains("SUCCESS");
        });
    }

    // --- Stub de l'événement ---
    record LocalTestEvent(String aggId) implements PricingEvent {
        public String eventId() { return UUID.randomUUID().toString(); }
        public String aggregateId() { return aggId; }
        public String module() { return "TEST_LOCAL"; }
        public String action() { return "VERIFICATION"; }
        public String actor() { return "moi"; }
        public String reason() { return "Test Local Sans Docker"; }
        public LocalDateTime occurredAt() { return LocalDateTime.now(); }
        public Map<String, Object> details() { return Map.of("result", "SUCCESS"); }
    }

}
