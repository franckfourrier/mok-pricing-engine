package com.kratos.mok.pricing.app.application.command.bankDeposit;

import com.kratos.mok.pricing.shared.domain.vo.EntityId;

import java.util.UUID;

public record CantonmentCreditId(String value) implements EntityId {

    public CantonmentCreditId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CantonmentCreditId cannot be null/blank");
        }
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for CantonmentCreditId: " + value);
        }
    }

    public static CantonmentCreditId generate() {
        return new CantonmentCreditId(UUID.randomUUID().toString());
    }

    public static CantonmentCreditId from(String uuidString) {
        return new CantonmentCreditId(uuidString);
    }

    @Override
    public String toString() {
        return value;
    }
}