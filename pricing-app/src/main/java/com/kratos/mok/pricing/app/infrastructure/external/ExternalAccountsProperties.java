package com.kratos.mok.pricing.app.infrastructure.external;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "external.accounts")
public record ExternalAccountsProperties(
        @NotBlank String baseUrl
) {}
