package com.kratos.mok.pricing.app.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
@Profile({"docker","prod"})
public class SecurityConfigProd {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfigProd.class);

    @Bean
    @Order(3)
    SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.POST, "/v1/fee-policies/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers(HttpMethod.POST, "/v1/tax-policies/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers(HttpMethod.POST, "/v1/commission-policies/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers(HttpMethod.POST, "/v1/ledger/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers(HttpMethod.POST, "/v1/reference/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String subject = jwt.getSubject();
            log.debug("[JWT-CONVERTER] subject={}", subject);

            Set<String> roles = new HashSet<>();

            // =========================
            // KEYCLOAK REALM ROLES
            // =========================
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> realmRoles) {
                realmRoles.forEach(r -> roles.add((String) r));
            }

            // =========================
            // KEYCLOAK CLIENT ROLES
            // =========================
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

            if (resourceAccess != null) {
                resourceAccess.forEach((client, value) -> {
                    if (value instanceof Map<?, ?> clientData) {
                        Object clientRoles = clientData.get("roles");
                        if (clientRoles instanceof List<?> list) {
                            list.forEach(r -> roles.add((String) r));
                        }
                    }
                });
            }

            // =========================
            // CONVERSION SPRING
            // =========================
            return roles.stream()
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .map(a -> (GrantedAuthority) a)
                    .toList();
        });

        return converter;
    }
}