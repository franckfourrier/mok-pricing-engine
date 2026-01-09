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

        // Validation : On refuse une précision excessive en entrée (ex: 10.555)
        // Cela force le développeur à choisir explicitement sa stratégie d'arrondi AVANT de créer l'objet.
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Maximum precision exceeded (2 decimal places) for value: " + amount);
        }

        // Normalisation : On force toujours 2 décimales (ex: 10 devient 10.00)
        // Le "amount =" modifie la valeur qui sera réellement stockée dans le record
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    // --- Factory Methods ---
    public static Money of(long value) {
        return new Money(BigDecimal.valueOf(value));
    }

    public static Money of(double value) {
        // Attention aux doubles, on passe par String pour la précision
        return new Money(BigDecimal.valueOf(value));
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }

    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    // --- Opérations Mathématiques ---

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(BigDecimal factor) {
        // Critique : On doit arrondir AVANT de recréer l'objet Money,
        // sinon le constructeur lancera une exception "Maximum precision exceeded".
        BigDecimal result = this.amount.multiply(factor)
                .setScale(2, RoundingMode.HALF_EVEN);
        return new Money(result);
    }

    // --- Helpers Logiques ---

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public int compareTo(Money o) {
        return this.amount.compareTo(o.amount);
    }

    @Override
    public String toString() {
        // On garde le format simple pour le débug
        return amount.toPlainString() + " XAF";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        // Comparaison par valeur numérique (10.00 == 10.0) et non par structure stricte
        return amount.compareTo(money.amount) == 0;
    }
}