package com.kratos.mok.pricing.app.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile({"docker","prod"})
public class WebSecurityConfigPublic {

    @Bean
    @Order(1)
    SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        // 🔓 Routes standards de Swagger et OpenAPI v3
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",

                        // 🔓 Routes alternatives avec préfixe /api (au cas où ton context-path ou gateway l'utilise)
                        "/api/swagger-ui.html",
                        "/api/swagger-ui/**",
                        "/api/v3/api-docs",
                        "/api/v3/api-docs/**"
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}