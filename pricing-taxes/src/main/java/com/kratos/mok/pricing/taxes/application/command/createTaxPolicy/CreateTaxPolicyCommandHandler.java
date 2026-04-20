package com.kratos.mok.pricing.taxes.application.command.createTaxPolicy;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.exception.ConflictException;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.TaxTarget;
import com.kratos.mok.pricing.taxes.domain.repository.TaxPolicyRepository;
import com.kratos.mok.pricing.taxes.domain.strategy.ElectronicRateTax;
import com.kratos.mok.pricing.taxes.domain.strategy.FixedAmountTax;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxRules;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxStrategy;
import com.kratos.mok.pricing.taxes.domain.vo.FluxIntensity;
import com.kratos.mok.pricing.taxes.domain.vo.TaxRate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CreateTaxPolicyCommandHandler {

    private final TaxPolicyRepository repository;
    private final TimeProvider timeProvider;

    @Transactional
    public CreateTaxPolicyResponse handle(CreateTaxPolicyCommand cmd, String actor) {

        OffsetDateTime now = timeProvider.now();

        if (cmd.transactionCodes() == null || cmd.transactionCodes().isEmpty()) {
            throw new DomainValidationException(
                    "TRANSACTION_CODES_REQUIRED",
                    "At least one transactionCode is required",
                    Map.of()
            );
        }

        Set<TransactionCode> transactionCodes = new LinkedHashSet<>(cmd.transactionCodes());

        if (transactionCodes.stream().anyMatch(tc -> !tc.supportsTaxes())) {
            throw new DomainValidationException(
                    "TAX_NOT_SUPPORTED_FOR_TRANSACTION",
                    "One or more transactionCodes do not support taxes",
                    Map.of("transactionCodes", transactionCodes.stream().map(Enum::name).toList())
            );
        }

        TaxTarget target = toTarget(cmd.targetScope(), cmd.targetValue());
        TaxStrategy strategy = toStrategy(cmd);
        TaxRules rules = toRules(cmd);

        TaxPolicy policy = TaxPolicy.draft(
                transactionCodes,
                target,
                cmd.mode(),
                strategy,
                rules,
                actor,
                now
        );

        if (repository.existsConflictingPolicy(policy)) {
            throw new ConflictException(
                    "Une taxe existe déjà pour au moins un transactionCode de cette sélection et ce périmètre"
            );
        }

        policy.submitForApproval(actor, now, "SUBMIT_FOR_APPROVAL");

        repository.save(policy);

        return new CreateTaxPolicyResponse(
                policy.id().value(),
                true,
                policy.status().name()
        );
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
        return switch (cmd.strategyType()) {
            case ELECTRONIC_RATE -> {
                if (cmd.rate() == null || cmd.rate().isBlank()) {
                    throw new DomainValidationException("RATE_REQUIRED", "rate is required", Map.of());
                }
                yield new ElectronicRateTax(new TaxRate(new BigDecimal(cmd.rate().trim())));
            }
            case FIXED_AMOUNT -> {
                if (cmd.fixedAmount() == null || cmd.fixedAmount().isBlank()) {
                    throw new DomainValidationException("FIXED_AMOUNT_REQUIRED", "fixedAmount is required", Map.of());
                }
                yield new FixedAmountTax(Money.of(cmd.fixedAmount(), cmd.currency()));
            }

            case NONE -> {
                yield null;
            }
        };
    }

    private TaxRules toRules(CreateTaxPolicyCommand cmd) {
        FluxIntensity intensity = (cmd.fluxIntensity() == null || cmd.fluxIntensity().isBlank())
                ? FluxIntensity.defaultOne()
                : new FluxIntensity(new BigDecimal(cmd.fluxIntensity().trim()));

        return new TaxRules(intensity, cmd.exempted());
    }

    private String required(String v, String msg) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(msg);
        return v;
    }
}