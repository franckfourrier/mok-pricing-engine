package com.kratos.mok.pricing.shared.domain.vo;

import java.io.Serializable;

public interface EntityId extends Serializable {
    String value();

    default String asString() {
        return value();
    }
}
