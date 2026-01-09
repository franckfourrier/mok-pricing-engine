package com.kratos.mok.pricing.fees.tests.unit.domain;

import com.kratos.mok.pricing.fees.domain.strategy.FeeStrategy;
import com.kratos.mok.pricing.fees.domain.strategy.FixedFee; // <--- Import
import com.kratos.mok.pricing.fees.domain.strategy.Tier;
import com.kratos.mok.pricing.fees.domain.strategy.TieredFee;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class TieredFeeTest {

    // On utilise la vraie stratégie FixedFee
    private final FeeStrategy fixed100 = new FixedFee(Money.of(100));
    private final FeeStrategy fixed500 = new FixedFee(Money.of(500));

    @Test
    void shouldCanDetectOverlapStrict() {
        // Palier A : 0 -> 1000
        // Palier B : 900 -> 2000 (Chevauchement car 900 < 1000)
        List<Tier> overlappingTiers = List.of(
                new Tier(Money.of(0), Money.of(1000), fixed100),
                new Tier(Money.of(900), Money.of(2000), fixed500)
        );

        assertThatThrownBy(() -> new TieredFee(overlappingTiers))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chevauchement réel détecté");
    }

    @Test
    void shouldAllowPerfectContinuity() {
        // Palier A : 0 -> 1000 (Exclusif)
        // Palier B : 1000 (Inclusif) -> 2000
        List<Tier> cleanTiers = List.of(
                new Tier(Money.of(0), Money.of(1000), fixed100),
                new Tier(Money.of(1000), Money.of(2000), fixed500)
        );

        assertThatCode(() -> new TieredFee(cleanTiers))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldApplyCorrectTierOnBoundary() {
        List<Tier> tiers = List.of(
                new Tier(Money.of(0), Money.of(1000), fixed100), // [0, 1000[
                new Tier(Money.of(1000), Money.of(2000), fixed500) // [1000, 2000[
        );
        TieredFee strategy = new TieredFee(tiers);

        // Cas 1 : 500 est dans le palier A
        assertThat(strategy.apply(Money.of(500))).isEqualTo(Money.of(100));

        // Cas 2 : 999 est encore dans le palier A (car < 1000)
        assertThat(strategy.apply(Money.of(999))).isEqualTo(Money.of(100));

        // Cas 3 : 1000 PILE tombe dans le palier B
        assertThat(strategy.apply(Money.of(1000))).isEqualTo(Money.of(500));
    }

    @Test
    void shouldThrowIfAmountNotCovered() {
        List<Tier> tiers = List.of(
                new Tier(Money.of(0), Money.of(1000), fixed100)
        );
        TieredFee strategy = new TieredFee(tiers);

        // 5000 n'est pas couvert
        assertThatThrownBy(() -> strategy.apply(Money.of(5000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ne correspond à aucun palier");
    }
}