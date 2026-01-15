package com.kratos.mok.pricing.shared.domain.vo;

import java.io.Serializable;
import java.util.UUID;

public interface EntityId extends Serializable {
    UUID value();

    default String asString() {
        return value().toString();
    }
}