package com.kratos.mok.pricing.fees.tests.unit.domain;

import com.kratos.mok.pricing.fees.domain.FeeLimits;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class FeeLimitsTest {

    @Test
    void shouldCanForbidIncoherentLimits() {
        // Min (1000) > Max (500) -> Impossible
        assertThatThrownBy(() -> new FeeLimits(Money.of(1000), Money.of(500)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ne peut pas être supérieur");
    }

    @Test
    void shouldCanApplyMinFloor() {
        // Min = 100, Max = 1000
        FeeLimits limits = new FeeLimits(Money.of(100), Money.of(1000));

        // Calcul théorique = 50 -> Doit payer 100 (Min)
        Money result = limits.apply(Money.of(50));
        assertThat(result).isEqualTo(Money.of(100));
    }

    @Test
    void shouldCanApplyMaxCeiling() {
        // Min = 100, Max = 1000
        FeeLimits limits = new FeeLimits(Money.of(100), Money.of(1000));

        // Calcul théorique = 5000 -> Doit payer 1000 (Max)
        Money result = limits.apply(Money.of(5000));
        assertThat(result).isEqualTo(Money.of(1000));
    }

    @Test
    void shouldCanKeepZeroFree() {
        // Règle : Si c'est gratuit (0), le minimum ne s'applique pas
        FeeLimits limits = new FeeLimits(Money.of(100), Money.of(1000));

        Money result = limits.apply(Money.ZERO);
        assertThat(result).isEqualTo(Money.ZERO);
    }
}
