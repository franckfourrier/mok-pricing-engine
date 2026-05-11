package com.kratos.mok.pricing.app.infrastructure.security.hmac;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NonceStore {

    private final Map<String, Long> nonces = new ConcurrentHashMap<>();

    public boolean registerIfAbsent(String partnerId, String nonce, long expiresAtEpochSeconds) {
        cleanupExpired();

        String key = partnerId + ":" + nonce;
        return nonces.putIfAbsent(key, expiresAtEpochSeconds) == null;
    }

    private void cleanupExpired() {
        long now = Instant.now().getEpochSecond();
        nonces.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}