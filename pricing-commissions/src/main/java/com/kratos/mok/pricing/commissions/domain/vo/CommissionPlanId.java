package com.kratos.mok.pricing.commissions.domain.vo;

import com.kratos.mok.pricing.shared.domain.vo.EntityId;

import java.util.UUID;

public record CommissionPlanId(String value) implements EntityId {

    public CommissionPlanId {

        if (value == null) {
            throw new IllegalArgumentException("CommissionPlanId cannot be null");
        }

        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for CommissionPlanId: " + value);
        }

    }

    public static CommissionPlanId generate() {
        return new CommissionPlanId(UUID.randomUUID().toString());
    }

    public static CommissionPlanId from(String uuidString) {
            return new CommissionPlanId(uuidString);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
