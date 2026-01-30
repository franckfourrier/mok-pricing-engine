package com.kratos.mok.pricing.fees.domain.vo;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;

public record PolicyPriority(int value) {

    public PolicyPriority {
        if (value < 0) throw new IllegalArgumentException("priority cannot be negative");
    }

    public static PolicyPriority of(int value) {
        return new PolicyPriority(value);
    }

    public static PolicyPriority defaultFor(TargetScope scope) {
        // valeurs par défaut, modifiables
        return switch (scope) {
            case GLOBAL -> new PolicyPriority(10);
            case ACCOUNT_TYPE -> new PolicyPriority(20);
            case ACCOUNT_ID -> new PolicyPriority(30);
        };
    }
}
