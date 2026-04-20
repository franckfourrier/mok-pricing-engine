package com.kratos.mok.pricing.app.bootstrap;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.FeeTarget;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.strategy.*;
import com.kratos.mok.pricing.fees.domain.vo.FeePercentage;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.Priority;
import com.kratos.mok.pricing.shared.domain.vo.ValidityPeriod;
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
    private final TimeProvider timeProvider;

    @Transactional
    public void bootstrap(FeeBootstrapProperties props) {

        var now = timeProvider.now();

        if (props.version() == null) {
            throw new IllegalArgumentException("bootstrap version is required");
        }

        final String ccy = (props.currency() == null || props.currency().isBlank())
                ? Money.DEFAULT_CURRENCY
                : props.currency().trim().toUpperCase();

        for (var y : props.fees()) {

            FeeTarget target = toTarget(y.target().scope(), y.target().value());
            TransactionCode transactionCode = y.transactionCode();

            if (repository.existsAnyFor(transactionCode, target.scope(), target.value())) {
                log.info("[BOOTSTRAP v{}] exists => skip: code={}, type={}, scope={}, value={}",
                        props.version(),
                        transactionCode,
                        transactionCode.transactionType(),
                        target.scope(),
                        target.value());
                continue;
            }

            FeeStrategy strategy = toStrategy(y, ccy);
            FeeRules rules = toRules(y, ccy);
            var priority = Priority.defaultFor(target.scope());

            FeePolicy policy = FeePolicy.bootstrapActive(
                    transactionCode,
                    transactionCode.transactionType(),
                    target,
                    strategy,
                    rules,
                    y.kycRequirement(),
                    ValidityPeriod.permanent(),
                    priority,
                    SYSTEM_ACTOR,
                    now
            );

            //repository.save(policy);

            log.info("[BOOTSTRAP v{}] created ACTIVE: id={}, code={}, type={}, scope={}, value={}",
                    props.version(),
                    policy.id().value(),
                    policy.transactionCode(),
                    policy.transactionType(),
                    target.scope(),
                    target.value());
        }
    }

    private FeeTarget toTarget(TargetScope scope, String value) {
        String v = required(value, "target.value is required").trim();
        return switch (scope) {
            case GLOBAL -> FeeTarget.global();
            case ACCOUNT_TYPE -> FeeTarget.accountType(v.toUpperCase());
            case ACCOUNT_ID -> FeeTarget.accountId(v);
        };
    }

    private FeeRules toRules(FeeBootstrapProperties.FeePolicyYaml y, String ccy) {
        Money threshold = money(required(y.activationThreshold(), "activationThreshold is required"), ccy);
        Money min = optionalMoney(y.minFee(), ccy);
        Money max = optionalMoney(y.maxFee(), ccy);
        return new FeeRules(threshold, min, max, y.minMonthlyTxCount());
    }

    private FeeStrategy toStrategy(FeeBootstrapProperties.FeePolicyYaml y, String ccy) {
        return switch (y.strategyType()) {
            case FIXED -> new FixedFee(money(required(y.fixedAmount(), "fixedAmount is required for FIXED"), ccy));

            case PROPORTIONAL -> {
                BigDecimal p = new BigDecimal(required(y.percentage(), "percentage is required for PROPORTIONAL"));
                yield new ProportionalFee(new FeePercentage(p));
            }

            case TIERED -> new TieredFee(toTiers(y.tiers(), ccy));
        };
    }

    private List<Tier> toTiers(List<FeeBootstrapProperties.TierYaml> tiers, String ccy) {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("tiers are required for TIERED");
        }
        return tiers.stream()
                .map(t -> new Tier(
                        money(required(t.min(), "tier.min required"), ccy),
                        money(required(t.max(), "tier.max required"), ccy),
                        toTierStrategy(t, ccy)
                ))
                .toList();
    }

    private FeeStrategy toTierStrategy(FeeBootstrapProperties.TierYaml t, String ccy) {
        return switch (t.tierStrategyType()) {
            case FIXED -> new FixedFee(money(required(t.tierValue(), "tierValue required for tier FIXED"), ccy));

            case PROPORTIONAL -> {
                BigDecimal p = new BigDecimal(required(t.tierValue(), "tierValue required for tier PROPORTIONAL"));
                yield new ProportionalFee(new FeePercentage(p));
            }

            case TIERED -> throw new IllegalArgumentException("Nested TIERED not allowed");
        };
    }

    private Money money(String value, String ccy) {
        return Money.of(value, ccy);
    }

    private Money optionalMoney(String v, String ccy) {
        return (v == null || v.isBlank()) ? null : Money.of(v, ccy);
    }

    private String required(String v, String msg) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(msg);
        return v;
    }
}