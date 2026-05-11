package com.kratos.mok.pricing.app.infrastructure.security.hmac;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public boolean allow(String key, int maxRequests, long windowSeconds) {
        long now = Instant.now().getEpochSecond();

        Counter counter = counters.compute(key, (k, existing) -> {
            if (existing == null || now >= existing.windowStart + windowSeconds) {
                return new Counter(now, 1);
            }

            existing.count++;
            return existing;
        });

        cleanup(now, windowSeconds);

        return counter.count <= maxRequests;
    }

    private void cleanup(long now, long windowSeconds) {
        counters.entrySet().removeIf(entry ->
                now >= entry.getValue().windowStart + windowSeconds
        );
    }

    private static class Counter {
        private final long windowStart;
        private int count;

        private Counter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}