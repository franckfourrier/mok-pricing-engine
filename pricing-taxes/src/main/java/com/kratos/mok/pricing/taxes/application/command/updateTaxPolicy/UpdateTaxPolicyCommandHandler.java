package com.kratos.mok.pricing.taxes.application.command.updateTaxPolicy;

import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.exception.ConflictException;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.taxes.domain.repository.TaxPolicyRepository;
import com.kratos.mok.pricing.taxes.domain.strategy.ElectronicRateTax;
import com.kratos.mok.pricing.taxes.domain.strategy.FixedAmountTax;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxRules;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxStrategy;
import com.kratos.mok.pricing.taxes.domain.vo.FluxIntensity;
import com.kratos.mok.pricing.taxes.domain.vo.TaxPolicyId;
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
public class UpdateTaxPolicyCommandHandler {

    private final TaxPolicyRepository repository;
    private final TimeProvider timeProvider;

    @Transactional
    public UpdateTaxPolicyResponse handle(UpdateTaxPolicyCommand cmd, String actor) {
        OffsetDateTime now = timeProvider.now();
        var policy = repository.findById(TaxPolicyId.from(cmd.policyId()))
                .orElseThrow(() -> new NotFoundException(
                        "TAX_POLICY_NOT_FOUND",
                        "TaxPolicy not found",
                        Map.of("id", cmd.policyId())
                ));

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

        TaxStrategy strategy = toStrategy(cmd, policy);
        TaxRules rules = toRules(cmd);

        policy.updateConfiguration(
                transactionCodes,
                cmd.mode(),
                strategy,
                rules,
                actor,
                now,
                "UPDATE_AND_RESUBMIT"
        );

        if (repository.existsConflictingPolicy(policy)) {
            throw new ConflictException(
                    "Une taxe existe déjà pour au moins un transactionCode de cette sélection et ce périmètre"
            );
        }

        repository.save(policy);

        return new UpdateTaxPolicyResponse(
                policy.id().value(),
                true,
                policy.status().name()
        );
    }

    private TaxStrategy toStrategy(UpdateTaxPolicyCommand cmd, com.kratos.mok.pricing.taxes.domain.TaxPolicy policy) {
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

                String currency = extractCurrency(policy);
                yield new FixedAmountTax(Money.of(cmd.fixedAmount(), currency));
            }
            case NONE -> {
                yield null;
            }
        };
    }

    private String extractCurrency(com.kratos.mok.pricing.taxes.domain.TaxPolicy policy) {
        if (policy.strategy() instanceof FixedAmountTax fixed) {
            return fixed.fixed().currency();
        }
        return Money.DEFAULT_CURRENCY;
    }

    private TaxRules toRules(UpdateTaxPolicyCommand cmd) {
        FluxIntensity intensity = (cmd.fluxIntensity() == null || cmd.fluxIntensity().isBlank())
                ? FluxIntensity.defaultOne()
                : new FluxIntensity(new BigDecimal(cmd.fluxIntensity().trim()));

        return new TaxRules(intensity, cmd.exempted());
    }
}
