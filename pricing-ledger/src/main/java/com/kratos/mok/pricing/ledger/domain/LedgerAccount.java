package com.kratos.mok.pricing.ledger.domain;

import com.kratos.mok.pricing.ledger.domain.vo.AccountCode;

import java.time.LocalDateTime;
import java.util.Objects;

public class LedgerAccount {

    private final String id;
    private final AccountCode code;
    private final String name;
    private final String currency;
    private final LocalDateTime createdAt;

    private LedgerAccount(String id, AccountCode code, String name, String currency, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.code = Objects.requireNonNull(code);
        this.name = Objects.requireNonNull(name);
        this.currency = Objects.requireNonNull(currency);
        this.createdAt = Objects.requireNonNull(createdAt);
        if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (currency.isBlank()) throw new IllegalArgumentException("currency must not be blank");
    }

    public static LedgerAccount reconstitute(String id, String code, String name, String currency, LocalDateTime createdAt) {
        return new LedgerAccount(id, AccountCode.of(code), name, currency.toUpperCase(), createdAt);
    }

    public String id() { return id; }
    public AccountCode code() { return code; }
    public String name() { return name; }
    public String currency() { return currency; }
    public LocalDateTime createdAt() { return createdAt; }
}
