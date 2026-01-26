/*
package com.kratos.mok.pricing.app.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/fee-policies").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    */
/**
     * Convertit les claims JWT -> GrantedAuthorities.
     *
     * Supporte les formats courants :
     * - roles: ["ADMIN","SUPER_ADMIN"]
     * - authorities: ["ROLE_ADMIN", ...]
     * - scope/scp: "admin super_admin" (optionnel)
     *//*

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 1) roles: ["ADMIN", "SUPER_ADMIN"]
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null && !roles.isEmpty()) {
                return roles.stream()
                        .filter(r -> r != null && !r.isBlank())
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .toList();
            }

            // 2) authorities: ["ROLE_ADMIN", ...]
            List<String> authorities = jwt.getClaimAsStringList("authorities");
            if (authorities != null && !authorities.isEmpty()) {
                return authorities.stream()
                        .filter(a -> a != null && !a.isBlank())
                        .map(SimpleGrantedAuthority::new)
                        .toList();
            }

            // 3) fallback: aucun rôle
            return List.of();
        });

        return converter;
    }
}

*/
