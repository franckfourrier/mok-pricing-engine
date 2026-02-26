package com.kratos.mok.pricing.app.infrastructure.config.swagger;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.regex.Pattern;

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
                .addOpenApiCustomizer(addBearerSecurityOnlyForSensitivePaths())
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
                .addOpenApiCustomizer(addBearerSecurityOnlyForSensitivePaths())
                .build();
    }

    @Bean
    GroupedOpenApi commissionsApi() {
        return GroupedOpenApi.builder()
                .group("commissions")
                .pathsToMatch(
                        "/v1/commissions/**",
                        "/v1/commission-plans/**",
                        "/v1/commission-policies/**"
                )
                .addOpenApiCustomizer(addBearerSecurityOnlyForSensitivePaths())
                .build();
    }

    @Bean
    GroupedOpenApi pricingApi() {
        return GroupedOpenApi.builder()
                .group("pricing")
                .pathsToMatch("/v1/pricing/**")
                .build();
    }

    private OpenApiCustomizer addBearerSecurityOnlyForSensitivePaths() {

        Set<String> actions = Set.of(
                "approve",
                "reject",
                "block",
                "unblock",
                "suspend",
                "resume",
                "archive",
                "restore",
                "cancel"
        );

        // match: /{id}/approve , /approve , /xxx/approve
        Pattern sensitivePattern = Pattern.compile(".*/(" + String.join("|", actions) + ")(/.*)?$");

        return openApi -> {
            if (openApi.getPaths() == null) return;

            var requirement = new SecurityRequirement().addList(OpenApiConfig.BEARER_AUTH);

            openApi.getPaths().forEach((path, item) -> {
                if (!sensitivePattern.matcher(path).matches()) return;
                applySecurity(requirement, item);
            });
        };
    }

    private void applySecurity(SecurityRequirement req, PathItem item) {
        if (item.getPost() != null) item.getPost().addSecurityItem(req);
        if (item.getPut() != null) item.getPut().addSecurityItem(req);
        if (item.getPatch() != null) item.getPatch().addSecurityItem(req);
        if (item.getDelete() != null) item.getDelete().addSecurityItem(req);
    }
}
