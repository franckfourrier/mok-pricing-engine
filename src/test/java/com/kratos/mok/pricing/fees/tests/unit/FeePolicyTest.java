package com.kratos.mok.pricing.fees.tests.unit;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.enums.AccountType;
import com.kratos.mok.pricing.fees.domain.enums.PolicyStatus;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.strategy.FixedFee;
import com.kratos.mok.pricing.fees.domain.strategy.ProportionalFee;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

public class FeePolicyTest {
    @Test
    void shouldCanCreateFeePolicy() {
        // GIVEN
        var strategy = new FixedFee(Money.of(100));

        // WHEN
        var policy = FeePolicy.create(
                TransactionType.WITHDRAWAL,
                AccountType.PREMIUM,
                strategy,
                "admin-1"
        );

        // THEN
        assertThat(policy.snapshot().status()).isEqualTo(PolicyStatus.PENDING_VALIDATION.name());
    }

    @Test
    void shouldCanNotCalculateFeeIfNotActive() {
        // GIVEN
        var policy = FeePolicy.create(TransactionType.WITHDRAWAL, AccountType.STANDARD, new FixedFee(Money.of(100)), "admin-1");

        // WHEN / THEN
        assertThatThrownBy(() -> policy.calculateFee(Money.of(5000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unable to apply an inactive rule");
    }

    @Test
    void shouldCanCalculateProportionalFeeAfterActivation() {
        // GIVEN: 1% de frais
        var strategy = new ProportionalFee(new BigDecimal("0.01"));
        var policy = FeePolicy.create(TransactionType.DEPOSIT, AccountType.PREMIUM, strategy, "admin-1");

        // ACT: Validation Super-Admin
        policy.activate("super-admin-X");

        // WHEN: Calcul sur 10.000 FCFA
        Money fee = policy.calculateFee(Money.of(10000));

        // THEN: 1% de 10.000 = 100 FCFA
        assertThat(policy.snapshot().status()).isEqualTo(PolicyStatus.ACTIVE.name());
        assertThat(fee).isEqualTo(Money.of(100));
    }
}