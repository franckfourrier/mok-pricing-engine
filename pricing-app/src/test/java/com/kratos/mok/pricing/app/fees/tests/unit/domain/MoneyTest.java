package com.kratos.mok.pricing.app.fees.tests.unit.domain;

import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCanAlwaysNormalizeScaleToTwoDecimals() {
        Money m1 = Money.of(10);
        Money m2 = Money.of("10.5");

        assertThat(m1.amount().scale()).isEqualTo(2); // 10.00
        assertThat(m2.amount().toString()).isEqualTo("10.50");
    }

    @Test
    void shouldCanRejectExcessivePrecision() {
        // Barrière 1 : On refuse de créer de l'argent avec 3 décimales
        assertThatThrownBy(() -> Money.of("10.555"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum precision exceeded");
    }

    @Test
    void shouldCanCalculateCorrectly() {
        Money a = Money.of(100);
        Money b = Money.of(50);

        assertThat(a.add(b)).isEqualTo(Money.of(150));
        assertThat(a.subtract(b)).isEqualTo(Money.of(50));
    }

    @Test
    void shouldCanHandleMultiplicationRounding() {
        // 10 * 1.555 = 15.55 (Arrondi HALF_EVEN)
        Money base = Money.of(10);
        Money result = base.multiply(new BigDecimal("1.555"));

        assertThat(result).isEqualTo(Money.of("15.55"));
    }
}