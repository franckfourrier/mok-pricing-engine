package com.kratos.mok.pricing.fees.domain.vo;

import com.kratos.mok.pricing.shared.domain.vo.EntityId;
import java.util.UUID;

public record FeePolicyId(String value) implements EntityId {

    public FeePolicyId {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FeePolicyId cannot be null/blank");
        }

        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for FeePolicyId: " + value);
        }
    }

    public static FeePolicyId generate() {
        return new FeePolicyId(UUID.randomUUID().toString());
    }

    public static FeePolicyId from(String uuidString) {
        return new FeePolicyId(uuidString);
    }

    @Override
    public String toString() {
        return value;
    }
}
