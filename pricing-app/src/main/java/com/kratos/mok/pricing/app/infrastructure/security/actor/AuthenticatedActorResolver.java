package com.kratos.mok.pricing.app.infrastructure.security.actor;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedActorResolver {

    public String resolve(Jwt jwt, String actorId) {

        if (actorId != null && !actorId.isBlank()) {
            return actorId.trim();
        }

        if (jwt == null) {
            return "UNKNOWN";
        }

        return firstNonBlank(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("client_id"),
                jwt.getClaimAsString("email"),
                "UNKNOWN"
        );
    }

    private String firstNonBlank(String... values) {

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }
}