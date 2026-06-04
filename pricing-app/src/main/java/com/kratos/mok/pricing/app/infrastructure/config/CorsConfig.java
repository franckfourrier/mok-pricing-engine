package com.kratos.mok.pricing.app.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:8003",
                "http://localhost:8080",
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:8002",
                "http://10.10.10.4:8002",
                "https://dashboardconformite.kpaymoney.com",
                "https://dashboardconformite.mokmoney.com",
                "https://dashboardconformitedev.kratosfinancialsinc.com",
                "https://pricing-dev.mok.kratosfinancialsinc.com"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS",
                "HEAD"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "X-Actor-Id",
                "X-Roles",
                "X-Partner-Id",
                "X-Signature",
                "X-Timestamp",
                "X-Nonce"
        ));

        configuration.setExposedHeaders(List.of(
                "X-Actor-Id",
                "X-Roles"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}