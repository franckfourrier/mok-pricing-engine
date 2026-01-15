package com.kratos.mok.pricing.fees.domain.vo;

import com.kratos.mok.pricing.shared.domain.vo.EntityId;

import java.util.UUID;

public record FeePolicyId(UUID value) implements EntityId {

    public FeePolicyId {
        if (value == null) {
            throw new IllegalArgumentException("FeePolicyId cannot be null");
        }
    }

    public static FeePolicyId generate() {
        return new FeePolicyId(UUID.randomUUID());
    }

    public static FeePolicyId from(String uuidString) {
        try {
            return new FeePolicyId(UUID.fromString(uuidString));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid format for FeePolicyId : " + uuidString);
        }
    }

    public static FeePolicyId from(UUID uuid) {
        return new FeePolicyId(uuid);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
