package com.kratos.mok.pricing.taxes.infrastructure.repository;

import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.TaxTarget;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import com.kratos.mok.pricing.taxes.domain.strategy.ElectronicRateTax;
import com.kratos.mok.pricing.taxes.domain.strategy.FixedAmountTax;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxRules;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxStrategy;
import com.kratos.mok.pricing.taxes.domain.vo.FluxIntensity;
import com.kratos.mok.pricing.taxes.domain.vo.TaxPolicyId;
import com.kratos.mok.pricing.taxes.domain.vo.TaxRate;
import com.kratos.mok.pricing.taxes.infrastructure.model.TaxPolicyEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Component
public class TaxPolicyEntityMapper {

    // -------------------------
    // Domain -> Entity
    // -------------------------
    public TaxPolicyEntity fromDomain(TaxPolicy d) {
        Objects.requireNonNull(d, "domain policy must not be null");

        TaxPolicyEntity e = new TaxPolicyEntity();

        // Identity & perimeter
        e.setId(d.id().value()); // TaxPolicyId is a String UUID
        e.setTransactionType(d.transactionType());
        e.setTargetScope(d.target().scope());
        e.setTargetValue(normalizeTargetValue(d.target()));

        // Core
        e.setMode(d.mode());

        // Strategy -> columns
        StrategyColumns sc = toStrategyColumns(d.strategy());
        e.setStrategyType(sc.type);
        e.setCurrency(sc.currency);
        e.setRate(sc.rate);
        e.setFixedAmount(sc.fixedAmount);

        // Rules
        FluxIntensity intensity = d.rules().intensity(); // defaultOne if null
        e.setFluxIntensity(intensity.value());
        e.setExempted(d.rules().isExempted());

        // Status
        e.setStatus(d.status());

        // Block reason (nullable)
        e.setBlockReason(d.blockReason());

        // Audit (created mandatory)
        AuditInfo created = d.created();
        e.setCreatedBy(created.author());
        e.setCreatedAt(created.timestamp());

        // lastModified (nullable)
        AuditInfo lm = d.lastModified();
        if (lm != null) {
            e.setLastModifiedBy(lm.author());
            e.setLastModifiedAt(lm.timestamp());
        } else {
            e.setLastModifiedBy(null);
            e.setLastModifiedAt(null);
        }

        // approvedOrRejected (nullable)
        AuditInfo ap = d.approvedOrRejected();
        if (ap != null) {
            e.setApprovedBy(ap.author());
            e.setApprovedAt(ap.timestamp());
        } else {
            e.setApprovedBy(null);
            e.setApprovedAt(null);
        }

        return e;
    }

    private String normalizeTargetValue(TaxTarget t) {
        String v = t.value().trim();
        return switch (t.scope()) {
            case ACCOUNT_TYPE -> v.toUpperCase();
            default -> v;
        };
    }

    private StrategyColumns toStrategyColumns(TaxStrategy s) {
        Objects.requireNonNull(s, "strategy must not be null");

        if (s instanceof ElectronicRateTax er) {
            return new StrategyColumns(
                    TaxStrategyType.ELECTRONIC_RATE,
                    Money.DEFAULT_CURRENCY,   // convention V1
                    er.rate().value(),
                    null
            );
        }

        if (s instanceof FixedAmountTax fa) {
            Money fixed = fa.fixed();
            return new StrategyColumns(
                    TaxStrategyType.FIXED_AMOUNT,
                    fixed.currency(),
                    null,
                    fixed.amount()
            );
        }

        throw new DomainValidationException(
                "UNSUPPORTED_TAX_STRATEGY",
                "Unsupported TaxStrategy implementation: " + s.getClass().getName(),
                Map.of("strategyClass", s.getClass().getName())
        );
    }

    // -------------------------
    // Entity -> Domain
    // -------------------------
    public TaxPolicy toDomain(TaxPolicyEntity e) {
        Objects.requireNonNull(e, "entity must not be null");

        TaxPolicyId id = TaxPolicyId.from(e.getId());

        TaxTarget target = new TaxTarget(e.getTargetScope(), e.getTargetValue());

        TaxStrategy strategy = fromStrategyColumns(
                e.getStrategyType(),
                e.getCurrency(),
                e.getRate(),
                e.getFixedAmount()
        );

        FluxIntensity intensity = (e.getFluxIntensity() == null)
                ? FluxIntensity.defaultOne()
                : new FluxIntensity(e.getFluxIntensity());

        TaxRules rules = new TaxRules(intensity, e.isExempted());

        AuditInfo created = new AuditInfo(
                requireNonBlank(e.getCreatedBy(), "createdBy"),
                requireNonNull(e.getCreatedAt(), "createdAt"),
                "PERSISTED"
        );

        AuditInfo lastModified = toAuditInfoNullable(e.getLastModifiedBy(), e.getLastModifiedAt(), "LAST_MODIFIED");
        AuditInfo approvedOrRejected = toAuditInfoNullable(e.getApprovedBy(), e.getApprovedAt(), "APPROVED_OR_REJECTED");

        return TaxPolicy.reconstitute(
                id,
                e.getTransactionType(),
                target,
                e.getMode(),
                strategy,
                rules,
                e.getStatus(),
                created,
                lastModified,
                approvedOrRejected,
                e.getBlockReason()
        );
    }

    private TaxStrategy fromStrategyColumns(
            TaxStrategyType type,
            String currency,
            BigDecimal rate,
            BigDecimal fixedAmount
    ) {
        if (type == null) {
            throw new DomainValidationException("STRATEGY_TYPE_REQUIRED", "strategyType is required", Map.of());
        }

        return switch (type) {
            case ELECTRONIC_RATE -> {
                if (rate == null) {
                    throw new DomainValidationException(
                            "RATE_REQUIRED",
                            "rate is required for ELECTRONIC_RATE",
                            Map.of("strategyType", "ELECTRONIC_RATE")
                    );
                }
                yield new ElectronicRateTax(new TaxRate(rate));
            }

            case FIXED_AMOUNT -> {
                if (fixedAmount == null) {
                    throw new DomainValidationException(
                            "FIXED_AMOUNT_REQUIRED",
                            "fixedAmount is required for FIXED_AMOUNT",
                            Map.of("strategyType", "FIXED_AMOUNT")
                    );
                }
                String c = (currency == null || currency.isBlank()) ? Money.DEFAULT_CURRENCY : currency;
                yield new FixedAmountTax(Money.of(fixedAmount, c));
            }
        };
    }

    private AuditInfo toAuditInfoNullable(String author, LocalDateTime at, String reason) {
        if (author == null || author.isBlank() || at == null) return null;
        return new AuditInfo(author, at, reason);
    }

    private static LocalDateTime requireNonNull(LocalDateTime v, String name) {
        if (v == null) throw new IllegalArgumentException(name + " must not be null");
        return v;
    }

    private static String requireNonBlank(String v, String name) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return v;
    }

    private record StrategyColumns(
            TaxStrategyType type,
            String currency,
            BigDecimal rate,
            BigDecimal fixedAmount
    ) {}
}
