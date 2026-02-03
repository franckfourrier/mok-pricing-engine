package com.kratos.mok.pricing.taxes.domain.vo;

import com.kratos.mok.pricing.shared.domain.vo.EntityId;
import java.util.UUID;

public record TaxPolicyId(String value) implements EntityId {

    public TaxPolicyId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TaxPolicyId cannot be null/blank");
        }

        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for TaxPolicyId: " + value);
        }
    }

    public static TaxPolicyId generate() {
        return new TaxPolicyId(UUID.randomUUID().toString());
    }

    public static TaxPolicyId from(String uuidString) {
        return new TaxPolicyId(uuidString);
    }

    @Override public String toString() {
        return value;
    }
}
