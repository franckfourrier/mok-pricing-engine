package com.kratos.mok.pricing.shared.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) implements Comparable<Money> {

    public static final String DEFAULT_CURRENCY = "XAF";
    public static final Money ZERO = new Money(BigDecimal.ZERO, DEFAULT_CURRENCY);

    public Money {
        Objects.requireNonNull(amount, "amount cannot be null");
        //Objects.requireNonNull(currency, "currency cannot be null");

        /*if (currency.isBlank()) {
            throw new IllegalArgumentException("currency cannot be blank");
        }*/

        if (currency == null || currency.isBlank()) {
            currency = DEFAULT_CURRENCY;
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Maximum precision exceeded (2 decimal places) for value: " + amount
            );
        }

        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
        currency = currency.toUpperCase();
    }

    // ---------- Factories ----------
    public static Money of(String value) {
        return new Money(new BigDecimal(value), DEFAULT_CURRENCY);
    }

    public static Money of(String value, String currency) {
        return new Money(new BigDecimal(value), currency);
    }

    public static Money of(double value) {
        return new Money(BigDecimal.valueOf(value), DEFAULT_CURRENCY);
    }

    public static Money of(double value, String currency) {
        return new Money(BigDecimal.valueOf(value), currency);
    }

    public static Money of(BigDecimal value) {
        return new Money(value, DEFAULT_CURRENCY);
    }

    public static Money of(BigDecimal value, String currency) {
        return new Money(value, currency);
    }


    // ---------- Operations ----------
    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), currency);
    }

    public Money multiply(BigDecimal factor) {
        BigDecimal result = this.amount
                .multiply(factor)
                .setScale(2, RoundingMode.HALF_EVEN);
        return new Money(result, currency);
    }

    // ---------- Guards ----------
    private void ensureSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currency + " vs " + other.currency
            );
        }
    }

    // ---------- Helpers ----------
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public int compareTo(Money o) {
        ensureSameCurrency(o);
        return amount.compareTo(o.amount);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return amount.compareTo(other.amount) == 0
                && currency.equals(other.currency);
    }
}
