package com.kratos.mok.pricing.app.infrastructure.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI mokPricingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MoK Pricing Engine API")
                        .version("v1")
                        .description("Fees / Taxes / Commissions + Pricing breakdown"))

                //schéma de sécurité
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));

                // applique l'auth par défaut à toute l'API
                //.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}