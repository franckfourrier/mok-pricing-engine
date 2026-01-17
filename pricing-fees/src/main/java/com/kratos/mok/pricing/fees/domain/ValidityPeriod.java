package com.kratos.mok.pricing.fees.domain;

import java.time.LocalDateTime;

public record ValidityPeriod(LocalDateTime start, LocalDateTime end) {

    public static final ValidityPeriod PERMANENT = new ValidityPeriod(null, null);

    public boolean isValidAt(LocalDateTime date) {
        if (start != null && date.isBefore(start)) return false;
        if (end != null && date.isAfter(end)) return false;
        return true;
    }
    public static ValidityPeriod permanent() {
        return PERMANENT;
    }
}