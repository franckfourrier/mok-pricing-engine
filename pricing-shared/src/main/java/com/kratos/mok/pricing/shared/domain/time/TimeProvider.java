package com.kratos.mok.pricing.shared.domain.time;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class TimeProvider {

    public OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}