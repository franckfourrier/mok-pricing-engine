package com.kratos.mok.pricing.app.fees.tests.unit;

import com.kratos.mok.pricing.fees.domain.*;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.snapshot.FeePolicySnapshot;
import com.kratos.mok.pricing.fees.domain.strategy.FeeStrategy;
import com.kratos.mok.pricing.fees.domain.strategy.FixedFee;
import com.kratos.mok.pricing.fees.domain.strategy.ProportionalFee;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FeePolicy Aggregate Tests")
class FeePolicyTest {

    // --- 1. CYCLE DE VIE & SNAPSHOT ---

    @Nested
    @DisplayName("Lifecycle & Snapshot")
    class LifecycleTests {

        @Test
        void shouldCanCreatePolicyFee() {
            // GIVEN
            var strategy = new FixedFee(Money.of(100));
            var target = FeeTarget.profile("PREMIUM");

            // WHEN
            var policy = FeePolicy.create(
                    TransactionType.WITHDRAWAL,
                    target,
                    strategy,
                    FeeLimits.NONE,
                    Money.ZERO,
                    ValidityPeriod.PERMANENT,
                    false,
                    "admin-1"
            );

            // THEN : On vérifie via le SNAPSHOT (Lecture seule)
            FeePolicySnapshot snapshot = policy.snapshot();

            assertThat(snapshot.id()).isNotNull();
            assertThat(snapshot.status()).isEqualTo("PENDING_VALIDATION");
            assertThat(snapshot.transactionType()).isEqualTo("WITHDRAWAL");
            assertThat(snapshot.targetScope()).isEqualTo("PROFILE");
            assertThat(snapshot.targetValue()).isEqualTo("PREMIUM");
            assertThat(snapshot.createdBy()).isEqualTo("admin-1");
            assertThat(snapshot.lastModifiedBy()).isNull();
        }

        @Test
        void shouldCanActivatePolicyFee() {
            // GIVEN
            var policy = FeePolicy.create(
                    TransactionType.WITHDRAWAL, FeeTarget.global(), new FixedFee(Money.of(100)),
                    FeeLimits.NONE, Money.ZERO, ValidityPeriod.PERMANENT, false, "admin-1"
            );

            // WHEN
            policy.activate("super-admin-X");

            // THEN
            FeePolicySnapshot snapshot = policy.snapshot();

            assertThat(snapshot.status()).isEqualTo("ACTIVE");
            assertThat(snapshot.lastModifiedBy()).isEqualTo("super-admin-X");
            assertThat(snapshot.lastModifiedDate()).isNotNull();
        }

        @Test
        void shouldCanSuspendPolicyFee() {
            // GIVEN
            var policy = createActivePolicy(new FixedFee(Money.of(100)));

            // WHEN
            policy.suspend("security-bot", "Suspicion de fraude");

            // THEN
            FeePolicySnapshot snapshot = policy.snapshot();
            assertThat(snapshot.status()).isEqualTo("SUSPENDED");
            assertThat(snapshot.lastModifiedBy()).contains("security-bot"); // Vérifie l'auteur
        }

        @Test
        void shouldCanNotCalculateFeeIfNotActive() {
            // GIVEN : Policy PENDING
            var policy = FeePolicy.create(
                    TransactionType.WITHDRAWAL, FeeTarget.global(), new FixedFee(Money.of(100)),
                    FeeLimits.NONE, Money.ZERO, ValidityPeriod.PERMANENT, false, "admin-1"
            );

            // WHEN / THEN
            assertThatThrownBy(() -> policy.calculateFee(TransactionContext.simple(Money.of(5000))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Rule not active");
        }
    }

    // --- 2. LOGIQUE DE CALCUL (MATHS) ---

    @Nested
    @DisplayName("Calculation Strategies")
    class StrategyTests {

        @Test
        void shouldCanCalculateFixedFee() {
            // GIVEN : 500 F fixe
            var policy = createActivePolicy(new FixedFee(Money.of(500)));

            // WHEN / THEN
            assertThat(policy.calculateFee(TransactionContext.simple(Money.of(1000))))
                    .isEqualTo(Money.of(500));

            assertThat(policy.calculateFee(TransactionContext.simple(Money.of(100000))))
                    .isEqualTo(Money.of(500));
        }

        @Test
        void shouldCanCalculateProportionalFee() {
            // GIVEN : 1%
            var policy = createActivePolicy(new ProportionalFee(new BigDecimal("0.01")));

            // WHEN : 10.000 F * 1% = 100 F
            Money fee = policy.calculateFee(TransactionContext.simple(Money.of(10000)));

            // THEN
            assertThat(fee).isEqualTo(Money.of(100));
        }
    }

    // --- 3. RÈGLES AVANCÉES (SEUILS & LIMITES) ---

    @Nested
    @DisplayName("Advanced Rules (Limits & Thresholds)")
    class AdvancedRulesTests {

        @Test
        void shouldCanApplyActivationThreshold() {
            // SCENARIO : Gratuit jusqu'à 5.000 F
            var activationThreshold = Money.of(5000);

            var policy = FeePolicy.create(
                    TransactionType.WITHDRAWAL, FeeTarget.global(), new FixedFee(Money.of(500)),
                    FeeLimits.NONE, activationThreshold, ValidityPeriod.PERMANENT, false, "admin"
            );
            policy.activate("super-admin");

            // CASE 1: Sous le seuil (4000) -> 0
            assertThat(policy.calculateFee(TransactionContext.simple(Money.of(4000))))
                    .isEqualTo(Money.ZERO);

            // CASE 2: Sur le seuil (5000) -> 0
            assertThat(policy.calculateFee(TransactionContext.simple(Money.of(5000))))
                    .isEqualTo(Money.ZERO);

            // CASE 3: Au dessus (5001) -> Payant (500)
            assertThat(policy.calculateFee(TransactionContext.simple(Money.of(5001))))
                    .isEqualTo(Money.of(500));
        }

        @Test
        void shouldCanRespectMinAndMaxLimits() {
            // SCENARIO : 1% avec Min 100 F et Max 1000 F
            var limits = new FeeLimits(Money.of(100), Money.of(1000));
            var strategy = new ProportionalFee(new BigDecimal("0.01"));

            var policy = FeePolicy.create(
                    TransactionType.WITHDRAWAL, FeeTarget.global(), strategy,
                    limits, Money.ZERO, ValidityPeriod.PERMANENT, false, "admin"
            );
            policy.activate("super-admin");

            // CASE 1: Petit montant (1.000 F) -> 1% = 10 F -> Plancher 100 F
            assertThat(policy.calculateFee(TransactionContext.simple(Money.of(1000))))
                    .isEqualTo(Money.of(100));

            // CASE 2: Montant moyen (50.000 F) -> 1% = 500 F -> OK
            assertThat(policy.calculateFee(TransactionContext.simple(Money.of(50000))))
                    .isEqualTo(Money.of(500));

            // CASE 3: Gros montant (200.000 F) -> 1% = 2.000 F -> Plafond 1.000 F
            assertThat(policy.calculateFee(TransactionContext.simple(Money.of(200000))))
                    .isEqualTo(Money.of(1000));
        }
    }

    // --- 4. CONTEXTE (DATES & KYC) ---

    @Nested
    @DisplayName("Context Rules (Date & KYC)")
    class ContextRulesTests {

        @Test
        void shouldCanEnforceValidityPeriod() {
            // GIVEN : Valide uniquement en 2024
            var validity2024 = new ValidityPeriod(
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    LocalDateTime.of(2024, 12, 31, 23, 59)
            );

            var policy = FeePolicy.create(
                    TransactionType.WITHDRAWAL, FeeTarget.global(), new FixedFee(Money.ZERO),
                    FeeLimits.NONE, Money.ZERO, validity2024, false, "admin"
            );
            policy.activate("super-admin");

            // CASE 1: Date valide
            var ctxValid = new TransactionContext(Money.of(100), LocalDateTime.of(2024, 6, 15, 12, 0), true, 0);
            assertThatCode(() -> policy.calculateFee(ctxValid)).doesNotThrowAnyException();

            // CASE 2: Date invalide (2025)
            var ctxInvalid = new TransactionContext(Money.of(100), LocalDateTime.of(2025, 1, 1, 12, 0), true, 0);
            assertThatThrownBy(() -> policy.calculateFee(ctxInvalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rule expired");
        }

        @Test
        void shouldCanEnforceKycRequirement() {
            // GIVEN : KYC Requis = true
            var policy = FeePolicy.create(
                    TransactionType.WITHDRAWAL, FeeTarget.global(), new FixedFee(Money.of(100)),
                    FeeLimits.NONE, Money.ZERO, ValidityPeriod.PERMANENT, true, "admin"
            );
            policy.activate("super-admin");

            // CASE 1: KYC OK
            var ctxKycOk = new TransactionContext(Money.of(1000), LocalDateTime.now(), true, 0);
            assertThatCode(() -> policy.calculateFee(ctxKycOk)).doesNotThrowAnyException();

            // CASE 2: KYC Manquant
            var ctxKycKo = new TransactionContext(Money.of(1000), LocalDateTime.now(), false, 0);
            assertThatThrownBy(() -> policy.calculateFee(ctxKycKo))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("KYC required");
        }
    }

    // --- 5. TESTS DE PRIORITÉ (HIÉRARCHIE) ---

    @Nested
    @DisplayName("Priority & Hierarchy")
    class PriorityTests {

        @Test
        void shouldCanCorrectlyExposePriority() {
            // GIVEN: 3 règles avec cibles différentes
            var global = FeePolicy.create(TransactionType.WITHDRAWAL, FeeTarget.global(), new FixedFee(Money.ZERO), FeeLimits.NONE, Money.ZERO, ValidityPeriod.PERMANENT, false, "admin");
            var profile = FeePolicy.create(TransactionType.WITHDRAWAL, FeeTarget.profile("VIP"), new FixedFee(Money.ZERO), FeeLimits.NONE, Money.ZERO, ValidityPeriod.PERMANENT, false, "admin");
            var individual = FeePolicy.create(TransactionType.WITHDRAWAL, FeeTarget.individual("User1"), new FixedFee(Money.ZERO), FeeLimits.NONE, Money.ZERO, ValidityPeriod.PERMANENT, false, "admin");

            // THEN: On vérifie que la logique interne attribue les bonnes priorités

            assertThat(global.priority()).isEqualTo(0);
            assertThat(profile.priority()).isEqualTo(1);
            assertThat(individual.priority()).isEqualTo(2);

            // Vérification via snapshot pour être sûr que l'API publique est cohérente
            assertThat(global.snapshot().targetScope()).isEqualTo("GLOBAL");
            assertThat(individual.snapshot().targetScope()).isEqualTo("INDIVIDUAL");
        }
    }

    // --- HELPER METHOD ---

    private FeePolicy createActivePolicy(FeeStrategy strategy) {
        var policy = FeePolicy.create(
                TransactionType.WITHDRAWAL,
                FeeTarget.global(),
                strategy,
                FeeLimits.NONE,
                Money.ZERO,
                ValidityPeriod.PERMANENT,
                false,
                "admin-test"
        );
        policy.activate("super-admin-test");
        return policy;
    }
}