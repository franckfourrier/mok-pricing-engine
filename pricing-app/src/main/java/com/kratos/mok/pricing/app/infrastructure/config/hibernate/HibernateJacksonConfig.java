package com.kratos.mok.pricing.app.infrastructure.config.hibernate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateJacksonConfig {

    @Bean
    HibernatePropertiesCustomizer jsonFormatMapper(ObjectMapper objectMapper) {
        return props -> props.put(
                AvailableSettings.JSON_FORMAT_MAPPER,
                new JacksonJsonFormatMapper(objectMapper)
        );
    }
}
