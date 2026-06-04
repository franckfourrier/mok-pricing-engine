package com.kratos.mok.pricing.app.infrastructure.security.dev;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
@Profile({"dev", "docker"})
public class SecurityConfigDev {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DevHeaderAuthFilter devHeaderAuthFilter
    ) throws Exception {

        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(
                        devHeaderAuthFilter,
                        AnonymousAuthenticationFilter.class
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    DevHeaderAuthFilter devHeaderAuthFilter() {
        return new DevHeaderAuthFilter();
    }
}