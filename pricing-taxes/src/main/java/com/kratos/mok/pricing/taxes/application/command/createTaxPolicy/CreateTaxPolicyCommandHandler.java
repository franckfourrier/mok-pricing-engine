package com.kratos.mok.pricing.taxes.application.command.createTaxPolicy;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.exception.ConflictException;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.TaxTarget;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.repository.TaxPolicyRepository;
import com.kratos.mok.pricing.taxes.domain.strategy.ElectronicRateTax;
import com.kratos.mok.pricing.taxes.domain.strategy.FixedAmountTax;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxRules;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxStrategy;
import com.kratos.mok.pricing.taxes.domain.vo.FluxIntensity;
import com.kratos.mok.pricing.taxes.domain.vo.TaxRate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTaxPolicyCommandHandler {

    private final TaxPolicyRepository repository;

    @Transactional
    public CreateTaxPolicyResponse handle(CreateTaxPolicyCommand cmd, String actor) {

        TaxTarget target = toTarget(cmd.targetScope(), cmd.targetValue());

        TaxStrategy strategy = toStrategy(cmd);

        FluxIntensity intensity = (cmd.fluxIntensity() == null || cmd.fluxIntensity().isBlank())
                ? FluxIntensity.defaultOne()
                : new FluxIntensity(new BigDecimal(cmd.fluxIntensity()));

        TaxRules rules = cmd.exempted()
                ? TaxRules.exemptedWithIntensity(intensity)
                : TaxRules.withIntensity(intensity);

        TaxMode mode = (cmd.mode() == null) ? TaxMode.CANTONNEMENT : cmd.mode();

        TaxPolicy policy = TaxPolicy.draft(
                cmd.type(),
                target,
                mode,
                strategy,
                rules,
                actor,
                LocalDateTime.now()
        );

        // Conflit V1: même (type + scope + value) déjà existant (non archivé)
        if (repository.existsConflictingPolicy(policy)) {
            throw new ConflictException("Un barème de taxe existe déjà pour ce périmètre.");
        }

        repository.save(policy);

        return new CreateTaxPolicyResponse(policy.id().value(), true, policy.status().name());
    }

    private TaxTarget toTarget(TargetScope scope, String value) {
        String v = required(value, "targetValue is required").trim();
        return switch (scope) {
            case GLOBAL -> TaxTarget.global();
            case ACCOUNT_TYPE -> TaxTarget.accountType(v);
            case ACCOUNT_ID -> TaxTarget.accountId(v);
        };
    }

    private TaxStrategy toStrategy(CreateTaxPolicyCommand cmd) {
        if (cmd.strategyType() == null) {
            throw new DomainValidationException("STRATEGY_TYPE_REQUIRED", "strategyType is required", Map.of());
        }

        return switch (cmd.strategyType()) {
            case ELECTRONIC_RATE -> {
                String r = required(cmd.rate(), "rate is required for ELECTRONIC_RATE");
                yield new ElectronicRateTax(new TaxRate(new BigDecimal(r)));
            }
            case FIXED_AMOUNT -> {
                String a = required(cmd.fixedAmount(), "fixedAmount is required for FIXED_AMOUNT");
                Money fixed = money(required(cmd.currency(), "currency is required"), a);
                yield new FixedAmountTax(fixed);
            }
        };
    }

    private Money money(String currency, String amount) {
        // adapte selon ta classe Money
        return Money.of(amount, currency);
    }

    private String required(String v, String msg) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(msg);
        return v;
    }
}
