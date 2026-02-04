package com.kratos.mok.pricing.ledger.domain.vo;

import java.util.Objects;

public record AccountCode(String value) {
    public AccountCode {
        Objects.requireNonNull(value, "accountCode must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("accountCode must not be blank");
    }

    public static AccountCode of(String v) { return new AccountCode(v.trim().toUpperCase()); }

    @Override public String toString() { return value; }
}
