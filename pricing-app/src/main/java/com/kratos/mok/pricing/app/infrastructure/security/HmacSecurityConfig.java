package com.kratos.mok.pricing.app.infrastructure.security;

import com.kratos.mok.pricing.app.infrastructure.security.hmac.PartnerHmacFilter;
import com.kratos.mok.pricing.app.infrastructure.security.hmac.PartnerSecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@Profile({"docker","prod"})
@EnableConfigurationProperties(PartnerSecurityProperties.class)
public class HmacSecurityConfig {

    @Bean
    @Order(2)
    SecurityFilterChain hmacSecurityFilterChain(
            HttpSecurity http,
            PartnerHmacFilter partnerHmacFilter
    ) throws Exception {

        return http
                .securityMatcher("/v1/pricing/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasRole("SYSTEM")
                )
                /*.authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )*/
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(
                        partnerHmacFilter,
                        AnonymousAuthenticationFilter.class
                )
                .build();
    }
}