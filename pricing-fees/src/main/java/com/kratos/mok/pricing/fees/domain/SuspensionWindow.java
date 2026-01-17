package com.kratos.mok.pricing.fees.domain;

import java.time.LocalDateTime;

public record SuspensionWindow(LocalDateTime from, LocalDateTime to) {

    public SuspensionWindow {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to cannot be null");
        }
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("to must be after from");
        }
    }

    public boolean isActiveAt(LocalDateTime at) {
        return !at.isBefore(from) && at.isBefore(to);
    }
}
