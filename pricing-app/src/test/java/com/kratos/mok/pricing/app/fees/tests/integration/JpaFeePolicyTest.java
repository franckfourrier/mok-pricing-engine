package com.kratos.mok.pricing.app.fees.tests.integration;

import com.kratos.mok.pricing.fees.domain.FeeLimits;
import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.FeeTarget;
import com.kratos.mok.pricing.fees.domain.ValidityPeriod;
import com.kratos.mok.pricing.fees.domain.enums.FeePolicyStatus;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.strategy.FixedFee;
import com.kratos.mok.pricing.fees.domain.strategy.ProportionalFee;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.fees.infrastructure.mapper.FeePolicyMapper;
import com.kratos.mok.pricing.fees.infrastructure.repository.PostgresFeePolicyRepository;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("local")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresFeePolicyRepository.class, FeePolicyMapper.class})
public class JpaFeePolicyTest {

    @Autowired
    private PostgresFeePolicyRepository repository;

    @Test
    @DisplayName("Save & Find: Doit persister la policy et désérialiser le JSONB correctement")
    void shouldCanSaveAndRetrievePolicy() {
        // GIVEN:
        var strategy = new ProportionalFee(new BigDecimal("0.025"));
        var limits = new FeeLimits(Money.of(100), Money.of(5000));
        var validity = new ValidityPeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));

        var policy = FeePolicy.create(
                TransactionType.WITHDRAWAL,
                FeeTarget.profile("VIP"),
                strategy,
                limits,
                Money.of(1000),
                validity,
                true,
                "admin-test"
        );
        policy.activate("super-admin");

        // WHEN:
        repository.save(policy);

        // THEN:
        var savedPolicy = repository.findById(policy.id());

        assertThat(savedPolicy).isPresent();
        var p = savedPolicy.get();

        assertThat(p.snapshot().id()).isEqualTo(policy.snapshot().id());
        assertThat(p.snapshot().status()).isEqualTo(FeePolicyStatus.ACTIVE.name());

        // Le test critique : Est-ce que le JSON a bien été reconverti en objet Java 'ProportionalFee' ?
        assertThat(p.strategy()).isInstanceOf(ProportionalFee.class);
        assertThat(((ProportionalFee) p.strategy()).percentage()).isEqualByComparingTo("0.025");

        // Vérification des Value Objects complexes
        assertThat(p.snapshot().limitsMin()).isEqualTo("100.00");
        assertThat(p.snapshot().kycRequired()).isTrue();
    }

    @Test
    @DisplayName("Query: Doit trouver les candidats actifs (Global + Profil + Individuel)")
    void should_find_matching_candidates() {
        // SCENARIO : On peuple la base

        // 1. Règle GLOBALE (Active) -> OK
        var globalPolicy = createPolicy(FeeTarget.global(), FeePolicyStatus.ACTIVE);
        repository.save(globalPolicy);

        // 2. Règle PROFILE "PREMIUM" (Active) -> OK pour Premium
        var premiumPolicy = createPolicy(FeeTarget.profile("PREMIUM"), FeePolicyStatus.ACTIVE);
        repository.save(premiumPolicy);

        // 3. Règle INDIVIDUAL "USER-123" (Active) -> OK pour User-123
        var userPolicy = createPolicy(FeeTarget.individual("USER-123"), FeePolicyStatus.ACTIVE);
        repository.save(userPolicy);

        // 4. Règle PROFILE "STANDARD" -> Ignore (mauvais profil)
        var standardPolicy = createPolicy(FeeTarget.profile("STANDARD"), FeePolicyStatus.ACTIVE);
        repository.save(standardPolicy);

        // 5. Règle GLOBALE (Suspendue) -> Ignore (statut)
        var suspendedPolicy = createPolicy(FeeTarget.global(), FeePolicyStatus.SUSPENDED);
        repository.save(suspendedPolicy);


        // --- TEST ---
        // On cherche les règles pour "USER-123" qui est "PREMIUM"
        List<FeePolicy> candidates = repository.findCandidates(
                TransactionType.WITHDRAWAL,
                "USER-123",
                "PREMIUM"
        );

        // --- ASSERTIONS ---
        assertThat(candidates).hasSize(3); // Global + Premium + User

        // On vérifie que ce sont bien des objets du domaine
        List<FeePolicyId> ids = candidates.stream().map(FeePolicy::id).toList();
        assertThat(ids).contains(globalPolicy.id(), premiumPolicy.id(), userPolicy.id());
        assertThat(ids).doesNotContain(standardPolicy.id(), suspendedPolicy.id());
    }

    // Helper pour créer rapidement des objets domaine
    private FeePolicy createPolicy(FeeTarget target, FeePolicyStatus status) {
        var p = FeePolicy.create(
                TransactionType.WITHDRAWAL, target, new FixedFee(Money.of(10)),
                FeeLimits.NONE, Money.ZERO, ValidityPeriod.PERMANENT, false, "setup"
        );
        if (status == FeePolicyStatus.ACTIVE) p.activate("setup");
        if (status == FeePolicyStatus.SUSPENDED) { p.activate("setup"); p.suspend("setup", "test"); }
        return p;
    }
}
