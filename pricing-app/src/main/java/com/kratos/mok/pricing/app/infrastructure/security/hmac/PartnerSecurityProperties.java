package com.kratos.mok.pricing.app.infrastructure.security.hmac;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "security.partner")
public record PartnerSecurityProperties(
        String id,
        String secret,
        long maxSkewSeconds,
        List<String> allowedIps,
        RateLimit rateLimit
) {
    public record RateLimit(
            int maxRequests,
            long windowSeconds
    ) {
    }
}