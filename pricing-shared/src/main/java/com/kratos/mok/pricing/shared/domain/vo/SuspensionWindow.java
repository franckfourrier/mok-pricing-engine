package com.kratos.mok.pricing.shared.domain.vo;

import java.time.OffsetDateTime;

public record SuspensionWindow(OffsetDateTime from, OffsetDateTime to) {

    public SuspensionWindow {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to cannot be null");
        }
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("to must be after from");
        }
    }

    public boolean isActiveAt(OffsetDateTime at) {
        return !at.isBefore(from) && at.isBefore(to);
    }
}
