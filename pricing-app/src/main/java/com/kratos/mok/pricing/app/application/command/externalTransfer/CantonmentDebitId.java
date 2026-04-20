package com.kratos.mok.pricing.app.application.command.externalTransfer;

import com.kratos.mok.pricing.shared.domain.vo.EntityId;

import java.util.UUID;

public record CantonmentDebitId(String value) implements EntityId {

    public CantonmentDebitId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CantonmentDebitId cannot be null/blank");
        }
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for CantonmentDebitId: " + value);
        }
    }

    public static CantonmentDebitId generate() {
        return new CantonmentDebitId(UUID.randomUUID().toString());
    }

    public static CantonmentDebitId from(String uuidString) {
        return new CantonmentDebitId(uuidString);
    }

    @Override
    public String toString() {
        return value;
    }
}