package com.kratos.mok.pricing.app.infrastructure.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Value("${mok.security.keycloak.server-url}")
    private String serverUrl;

    @Value("${mok.security.keycloak.realm}")
    private String realm;

    @Value("${mok.security.keycloak.client-id}")
    private String clientId;

    @Bean
    public OpenAPI mokPricingOpenApi() {

        String authUrl =
                serverUrl +
                        "/realms/" +
                        realm +
                        "/protocol/openid-connect/auth";

        String tokenUrl =
                serverUrl +
                        "/realms/" +
                        realm +
                        "/protocol/openid-connect/token";

        return new OpenAPI()
                .info(new Info()
                        .title("MoK Pricing Engine API")
                        .version("v1")
                        .description("Fees / Taxes / Commissions + Pricing breakdown"))

                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_AUTH,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(
                                                new OAuthFlows()
                                                        .authorizationCode(
                                                                new OAuthFlow()
                                                                        .authorizationUrl(authUrl)
                                                                        .tokenUrl(tokenUrl)
                                                        )
                                        )
                        ))
                .addSecurityItem(
                        new SecurityRequirement().addList(BEARER_AUTH)
                );
    }
}