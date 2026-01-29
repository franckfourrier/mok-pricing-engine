package com.kratos.mok.pricing.app.infrastructure.config.swagger;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroupsConfig {

    @Bean
    GroupedOpenApi feesApi() {
        return GroupedOpenApi.builder()
                .group("fees")
                .pathsToMatch(
                        "/v1/fees/**",
                        "/v1/fee-policies/**"
                )
                .build();
    }

    @Bean
    GroupedOpenApi taxesApi() {
        return GroupedOpenApi.builder()
                .group("taxes")
                .pathsToMatch(
                        "/v1/taxes/**",
                        "/v1/tax-policies/**"
                )
                .build();
    }

    @Bean
    GroupedOpenApi commissionsApi() {
        return GroupedOpenApi.builder()
                .group("commissions")
                .pathsToMatch(
                        "/v1/commissions/**",
                        "/v1/commission-policies/**"
                )
                .build();
    }

    @Bean
    GroupedOpenApi pricingApi() {
        return GroupedOpenApi.builder()
                .group("pricing")
                .pathsToMatch("/v1/pricing/**")
                .build();
    }
}
