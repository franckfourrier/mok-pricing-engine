package com.kratos.mok.pricing.shared.infrastructure.config;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.infrastructure.config.jackson.MoneyMixin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    // On crée MANUELLEMENT l'ancien ObjectMapper pour satisfaire AuditListener
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Gestion des dates (JavaTimeModule est vital pour LocalDateTime)
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 2. Robustesse
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 3. Notre Mixin pour Money
        mapper.addMixIn(Money.class, MoneyMixin.class);

        return mapper;
    }
}