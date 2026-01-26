package com.kratos.mok.pricing.app.bootstrap;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.FeeTarget;
import com.kratos.mok.pricing.fees.domain.ValidityPeriod;
import com.kratos.mok.pricing.fees.domain.enums.TargetScope;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.strategy.*;
import com.kratos.mok.pricing.fees.domain.vo.FeePercentage;
import com.kratos.mok.pricing.fees.domain.vo.PolicyPriority;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeePolicyBootstrapService {

    public static final String SYSTEM_ACTOR = "SYSTEM_BOOTSTRAP";

    private final FeePolicyRepository repository;

    @Transactional
    public void bootstrap(FeeBootstrapProperties props) {

        if (props.version() == null) {
            throw new IllegalArgumentException("bootstrap version is required");
        }

        var now = LocalDateTime.now();

        for (var y : props.fees()) {

            FeeTarget target = toTarget(y.target().scope(), y.target().value());

            if (repository.existsAnyFor(y.transactionType(), target.scope(), target.value())) {
                log.info("[BOOTSTRAP v{}] exists => skip: type={}, scope={}, value={}",
                        props.version(), y.transactionType(), target.scope(), target.value());
                continue;
            }

            FeeStrategy strategy = toStrategy(y);
            FeeRules rules = toRules(y);
            var priority = PolicyPriority.defaultFor(target.scope());

            FeePolicy policy = FeePolicy.bootstrapActive(
                    y.transactionType(),
                    target,
                    strategy,
                    rules,
                    y.kycRequirement(),
                    ValidityPeriod.permanent(),
                    priority,
                    SYSTEM_ACTOR,
                    now
            );

            repository.save(policy);

            log.info("[BOOTSTRAP v{}] created ACTIVE: id={}, type={}, scope={}, value={}",
                    props.version(), policy.id().value(), y.transactionType(), target.scope(), target.value());
        }
    }

    private FeeTarget toTarget(TargetScope scope, String value) {
        String v = required(value, "target.value is required").trim();
        return switch (scope) {
            case GLOBAL -> FeeTarget.global();
            case ACCOUNT_TYPE -> FeeTarget.accountType(v);
            case ACCOUNT_ID -> FeeTarget.accountId(v);
        };
    }

    private FeeRules toRules(FeeBootstrapProperties.FeePolicyYaml y) {
        Money threshold = Money.of(required(y.activationThreshold(), "activationThreshold is required"));
        Money min = optionalMoney(y.minFee());
        Money max = optionalMoney(y.maxFee());

        return new FeeRules(threshold, min, max, y.minMonthlyTxCount());
    }

    private FeeStrategy toStrategy(FeeBootstrapProperties.FeePolicyYaml y) {
        return switch (y.strategyType()) {
            case FIXED -> new FixedFee(Money.of(required(y.fixedAmount(), "fixedAmount is required for FIXED")));
            case PROPORTIONAL -> {
                BigDecimal p = new BigDecimal(required(y.percentage(), "percentage is required for PROPORTIONAL"));
                yield new ProportionalFee(new FeePercentage(p));
            }
            case TIERED -> new TieredFee(toTiers(y.tiers()));
        };
    }

    private List<Tier> toTiers(List<FeeBootstrapProperties.TierYaml> tiers) {
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

    private FeeStrategy toTierStrategy(FeeBootstrapProperties.TierYaml t) {
        return switch (t.tierStrategyType()) {
            case FIXED -> new FixedFee(Money.of(required(t.tierValue(), "tierValue required for tier FIXED")));
            case PROPORTIONAL -> {
                BigDecimal p = new BigDecimal(required(t.tierValue(), "tierValue required for tier PROPORTIONAL"));
                yield new ProportionalFee(new FeePercentage(p));
            }
            case TIERED -> throw new IllegalArgumentException("Nested TIERED not allowed");
        };
    }

    private Money optionalMoney(String v) {
        return (v == null || v.isBlank()) ? null : Money.of(v);
    }

    private String required(String v, String msg) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(msg);
        return v;
    }
}
