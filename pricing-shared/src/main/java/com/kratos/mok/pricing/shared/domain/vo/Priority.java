package com.kratos.mok.pricing.shared.domain.vo;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;

public record Priority(int value) {

    public Priority {
        if (value < 0) throw new IllegalArgumentException("priority cannot be negative");
    }

    public static Priority of(int value) {
        return new Priority(value);
    }

    public static Priority defaultFor(TargetScope scope) {
        // valeurs par défaut, modifiables
        return switch (scope) {
            case GLOBAL -> new Priority(10);
            case ACCOUNT_TYPE -> new Priority(20);
            case ACCOUNT_ID -> new Priority(30);
        };
    }
}
