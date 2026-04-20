package com.kratos.mok.pricing.shared.domain.vo;

import java.time.OffsetDateTime;

public record ValidityPeriod(OffsetDateTime start, OffsetDateTime end) {

    public static final ValidityPeriod PERMANENT = new ValidityPeriod(null, null);

    public boolean isValidAt(OffsetDateTime date) {
        if (start != null && date.isBefore(start)) return false;
        if (end != null && date.isAfter(end)) return false;
        return true;
    }
    public static ValidityPeriod permanent() {
        return PERMANENT;
    }
}