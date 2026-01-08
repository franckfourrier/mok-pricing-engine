package com.kratos.mok.pricing.shared.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) implements Comparable<Money> {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("The amount cannot be null");
        }
        // XAF n'a pas de centimes généralement, mais on garde 2 décimales pour les calculs internes
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Maximum precision exceeded (2 decimal places)");
        }
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Money of(long value) {
        return new Money(BigDecimal.valueOf(value));
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(BigDecimal factor) {
        // Arrondi bancaire (HALF_EVEN) pour minimiser les erreurs cumulées
        return new Money(this.amount.multiply(factor).setScale(2, RoundingMode.HALF_EVEN));
    }
    @JsonIgnore
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public int compareTo(Money o) {
        return this.amount.compareTo(o.amount);
    }

    // Pour l'affichage JSON ou Logs
    @Override
    public String toString() {
        return amount.toPlainString() + " XAF";
    }
}