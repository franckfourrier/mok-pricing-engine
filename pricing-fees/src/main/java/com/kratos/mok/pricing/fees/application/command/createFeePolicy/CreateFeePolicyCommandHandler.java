package com.kratos.mok.pricing.fees.application.command.createFeePolicy;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.FeeTarget;
import com.kratos.mok.pricing.fees.domain.enums.KycRequirement;
import com.kratos.mok.pricing.fees.domain.event.FeePolicyCreatedEvent;
import com.kratos.mok.pricing.fees.domain.gateway.RegulatoryGatekeeper;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.strategy.*;
import com.kratos.mok.pricing.fees.domain.vo.FeePercentage;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.event.ConfigurationBlockedEvent;
import com.kratos.mok.pricing.shared.domain.exception.ConflictException;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.RegulatoryViolationException;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.Priority;
import com.kratos.mok.pricing.shared.domain.vo.ValidityPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateFeePolicyCommandHandler {

    private final FeePolicyRepository repository;
    private final RegulatoryGatekeeper regulatoryGatekeeper;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeProvider timeProvider;

    @Transactional
    public CreateFeePolicyResponse handle(CreateFeePolicyCommand cmd, String authorId) {

        var now = timeProvider.now();
        log.info("CreateFeePolicy: code={}, type={}, scope={}, value={}",
                cmd.transactionCode(),
                cmd.transactionCode().transactionType(),
                cmd.targetScope(),
                cmd.targetValue());

        FeeTarget target = toTarget(cmd.targetScope(), cmd.targetValue());
        FeeStrategy strategy = toStrategy(cmd);

        String currency = required(cmd.currency(), "currency is required");

        Money threshold = Money.of(required(cmd.activationThreshold(), "activationThreshold is required"), currency);
        Money min = toOptionalMoney(cmd.minFee(), currency);
        Money max = toOptionalMoney(cmd.maxFee(), currency);

        FeeRules rules = new FeeRules(threshold, min, max, cmd.minMonthlyTxCount());

        ValidityPeriod validity = (cmd.validityStart() == null && cmd.validityEnd() == null)
                ? ValidityPeriod.PERMANENT
                : new ValidityPeriod(cmd.validityStart(), cmd.validityEnd());

        KycRequirement kyc = (cmd.kycRequirement() == null) ? KycRequirement.NONE : cmd.kycRequirement();

        var priority = Priority.defaultFor(target.scope());

        FeePolicy policy = FeePolicy.draft(
                cmd.transactionCode(),
                cmd.transactionCode().transactionType(),
                target,
                strategy,
                rules,
                kyc,
                validity,
                priority,
                authorId,
                now
        );

        if (repository.existsConflictingPolicy(policy)) {
            block(policy, authorId, "CONFLICTING_POLICY", "Policy conflict", cmd);
            throw new ConflictException("Une politique active existe déjà pour ce périmètre");
        }

        try {
            regulatoryGatekeeper.validate(policy.toComplianceData());
        } catch (RegulatoryViolationException e) {
            block(policy, authorId, e.regulationCode(), e.getMessage(), cmd);
            throw new DomainValidationException(
                    "BEAC_NON_COMPLIANT",
                    e.getMessage(),
                    Map.of("regulationCode", e.regulationCode())
            );
        }

        policy.submitForApproval(authorId, now, "SUBMIT_FOR_APPROVAL");

        repository.save(policy);

        eventPublisher.publishEvent(new FeePolicyCreatedEvent(
                policy.id().value(), authorId, now));

        return new CreateFeePolicyResponse(policy.id().value(), true, policy.status());
    }

    private void block(FeePolicy p, String actor, String code, String reason, CreateFeePolicyCommand cmd) {
        var now = timeProvider.now();
        p.block(code, reason, actor, now);
        repository.save(p);

        eventPublisher.publishEvent(new ConfigurationBlockedEvent(
                p.id().value(),
                "FEE_POLICY",
                actor,
                code,
                reason,
                Map.of(
                        "transactionCode", cmd.transactionCode().name(),
                        "transactionType", cmd.transactionCode().transactionType().name(),
                        "scope", cmd.targetScope().name(),
                        "value", cmd.targetValue()
                ),
                now
        ));
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
            case FIXED -> {
                if (cmd.fixedAmount() == null || cmd.fixedAmount().isBlank()) {
                    throw new DomainValidationException(
                            "FIXED_AMOUNT_REQUIRED",
                            "fixedAmount is required for FIXED strategy",
                            Map.of("strategyType", "FIXED")
                    );
                }
                yield new FixedFee(Money.of(cmd.fixedAmount(), cmd.currency()));
            }
            case PROPORTIONAL -> {
                if (cmd.percentage() == null || cmd.percentage().isBlank()) {
                    throw new DomainValidationException(
                            "PERCENTAGE_REQUIRED",
                            "percentage is required for PROPORTIONAL strategy",
                            Map.of("strategyType", "PROPORTIONAL")
                    );
                }
                BigDecimal percent = new BigDecimal(cmd.percentage());
                yield new ProportionalFee(new FeePercentage(percent));
            }
            case TIERED -> {
                if (cmd.tiers() == null || cmd.tiers().isEmpty()) {
                    throw new DomainValidationException(
                            "TIERS_REQUIRED",
                            "tiers are required for TIERED strategy",
                            Map.of("strategyType", "TIERED")
                    );
                }
                yield new TieredFee(toTiers(cmd.tiers(), cmd.currency()));
            }
        };
    }

    private List<Tier> toTiers(List<CreateFeePolicyCommand.TierCommand> tiers, String currency) {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("tiers are required for TIERED");
        }
        return tiers.stream()
                .map(t -> new Tier(
                        Money.of(required(t.min(), "tier.min required"), currency),
                        Money.of(required(t.max(), "tier.max required"), currency),
                        toTierStrategy(t, currency)
                ))
                .toList();
    }

    private FeeStrategy toTierStrategy(CreateFeePolicyCommand.TierCommand t, String currency) {
        return switch (t.tierStrategyType()) {
            case FIXED -> new FixedFee(Money.of(required(t.tierValue(), "tierValue required for tier FIXED"), currency));
            case PROPORTIONAL -> {
                BigDecimal p = new BigDecimal(required(t.tierValue(), "tierValue required for tier PROPORTIONAL"));
                yield new ProportionalFee(new FeePercentage(p));
            }
            case TIERED -> throw new IllegalArgumentException("Nested TIERED is not allowed");
        };
    }

    private Money toOptionalMoney(String v, String currency) {
        return (v == null || v.isBlank()) ? null : Money.of(v, currency);
    }

    private String required(String v, String msg) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(msg);
        return v;
    }
}