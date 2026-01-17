package com.kratos.mok.pricing.fees.application.command.createFeePolicy;

import com.kratos.mok.pricing.control.domain.event.ConfigurationBlockedEvent;
import com.kratos.mok.pricing.fees.domain.gateway.RegulatoryViolationException;
import com.kratos.mok.pricing.fees.domain.FeeTarget;
import com.kratos.mok.pricing.fees.domain.ValidityPeriod;
import com.kratos.mok.pricing.fees.domain.enums.KycRequirement;
import com.kratos.mok.pricing.fees.domain.enums.TargetScope;
import com.kratos.mok.pricing.fees.domain.event.FeePolicyCreatedEvent;
import com.kratos.mok.pricing.fees.domain.gateway.RegulatoryGatekeeper;
import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.strategy.*;
import com.kratos.mok.pricing.fees.domain.vo.FeePercentage;
import com.kratos.mok.pricing.fees.domain.vo.PolicyPriority;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateFeePolicyHandler {

    private final FeePolicyRepository repository;
    private final RegulatoryGatekeeper regulatoryGatekeeper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateFeePolicyResponse handle(CreateFeePolicyCommand cmd, String authorId) {
        log.info("CreateFeePolicy: type={}, scope={}, value={}", cmd.type(), cmd.targetScope(), cmd.targetValue());

        // ✅ 1) Construire Value Objects (Barrière 1)
        FeeTarget target = toTarget(cmd.targetScope(), cmd.targetValue());
        FeeStrategy strategy = toStrategy(cmd);

        Money threshold = Money.of(required(cmd.activationThreshold(), "activationThreshold is required"));
        Money min = toOptionalMoney(cmd.minFee());
        Money max = toOptionalMoney(cmd.maxFee());

        FeeRules rules = new FeeRules(threshold, min, max, cmd.minMonthlyTxCount());

        ValidityPeriod validity = (cmd.validityStart() == null && cmd.validityEnd() == null)
                ? ValidityPeriod.PERMANENT
                : new ValidityPeriod(cmd.validityStart(), cmd.validityEnd());

        KycRequirement kyc = (cmd.kycRequirement() == null) ? KycRequirement.NONE : cmd.kycRequirement();

        // priorité par défaut basée sur scope
        var priority = PolicyPriority.defaultFor(target.scope());


        // ✅ 2) Création domaine
        FeePolicy policy = FeePolicy.draft(
                cmd.type(),
                target,
                strategy,
                rules,
                kyc,
                validity,
                priority,
                authorId,
                LocalDateTime.now()
        );


        // 🔴 Barrière 2 — Conflit = BLOCAGE
        if (repository.existsConflictingPolicy(policy)) {

            policy.block(
                    "CONFLICTING_POLICY",
                    "Une politique active existe déjà pour ce périmètre",
                    authorId
            );

            repository.save(policy);

            eventPublisher.publishEvent(
                    new ConfigurationBlockedEvent(
                            policy.id().value(),
                            "FEE_POLICY",
                            authorId,
                            "CONFLICTING_POLICY",
                            "Conflit avec une politique existante",
                            java.util.Map.of(
                                    "transactionType", cmd.type().name(),
                                    "scope", cmd.targetScope().name(),
                                    "value", cmd.targetValue()
                            )
                    )
            );

            return new CreateFeePolicyResponse(policy.id().value(), false, policy.status());
        }

        // 🔴 Barrière 3 — Non conformité BEAC = BLOCAGE
        try {
            regulatoryGatekeeper.validate(policy.toComplianceData());
        } catch (RegulatoryViolationException e) {
            policy.block("BEAC_NON_COMPLIANT", e.getMessage(), authorId);
            repository.save(policy);

            eventPublisher.publishEvent(
                    new ConfigurationBlockedEvent(
                            policy.id().value(),
                            "FEE_POLICY",
                            authorId,
                            e.regulationCode(),
                            e.getMessage(),
                            java.util.Map.of("regulationCode", e.regulationCode()
                            )
                    )
            );

            return new CreateFeePolicyResponse(policy.id().value(), false, policy.status());
        }

        // ✅ Création valide → en attente de validation
        policy.submitForApproval(authorId, LocalDateTime.now(), "SUBMIT_FOR_APPROVAL");

        repository.save(policy);

        eventPublisher.publishEvent(
                new FeePolicyCreatedEvent(
                        policy.id().value(),
                        authorId,
                        LocalDateTime.now()
                )
        );

        return new CreateFeePolicyResponse(policy.id().value(), true, policy.status()
        );
    }

    private FeeTarget toTarget(TargetScope scope, String value) {
        String v = required(value, "targetValue is required").trim();
        return switch (scope) {
            case GLOBAL -> FeeTarget.global();
            case ACCOUNT_TYPE -> FeeTarget.accountType(v);
            case ACCOUNT_ID -> FeeTarget.accountId(v);
        };
    }

    private FeeStrategy toStrategy(CreateFeePolicyCommand cmd) {
        return switch (cmd.strategyType()) {
            case FIXED -> new FixedFee(Money.of(required(cmd.fixedAmount(), "fixedAmount is required for FIXED")));
            case PROPORTIONAL -> {
                BigDecimal percent = new BigDecimal(required(cmd.percentage(), "percentage is required for PROPORTIONAL"));
                yield new ProportionalFee(new FeePercentage(percent));
            }
            case TIERED -> new TieredFee(toTiers(cmd.tiers()));
        };
    }

    private java.util.List<Tier> toTiers(java.util.List<CreateFeePolicyCommand.TierCommand> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("tiers are required for TIERED");
        }
        return tiers.stream()
                .map(t -> new Tier(
                        Money.of(required(t.min(), "tier.min required")),
                        Money.of(required(t.max(), "tier.max required")),
                        toTierStrategy(t)
                ))
                .toList();
    }

    private FeeStrategy toTierStrategy(CreateFeePolicyCommand.TierCommand t) {
        return switch (t.tierStrategyType()) {
            case FIXED -> new FixedFee(Money.of(required(t.tierValue(), "tierValue required for tier FIXED")));
            case PROPORTIONAL -> {
                BigDecimal p = new BigDecimal(required(t.tierValue(), "tierValue required for tier PROPORTIONAL"));
                yield new ProportionalFee(new FeePercentage(p));
            }
            case TIERED -> throw new IllegalArgumentException("Nested TIERED is not allowed");
        };
    }

    private Money toOptionalMoney(String v) {
        return (v == null || v.isBlank()) ? null : Money.of(v);
    }

    private String required(String v, String msg) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(msg);
        return v;
    }
}
