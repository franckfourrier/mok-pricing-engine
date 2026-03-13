package com.kratos.mok.pricing.taxes.infrastructure.mapper;

import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.TaxTarget;
import com.kratos.mok.pricing.taxes.domain.enums.TaxPolicyStatus;
import com.kratos.mok.pricing.taxes.domain.strategy.ElectronicRateTax;
import com.kratos.mok.pricing.taxes.domain.strategy.FixedAmountTax;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxRules;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxStrategy;
import com.kratos.mok.pricing.taxes.domain.vo.FluxIntensity;
import com.kratos.mok.pricing.taxes.domain.vo.TaxPolicyId;
import com.kratos.mok.pricing.taxes.domain.vo.TaxRate;
import com.kratos.mok.pricing.taxes.infrastructure.model.TaxPolicyEntity;
import org.springframework.stereotype.Component;

@Component
public class TaxPolicyEntityMapper {

    public TaxPolicyEntity fromDomain(TaxPolicy p) {

        TaxPolicyEntity e = new TaxPolicyEntity();

        e.setId(p.id().value());
        e.setTransactionCodes(new java.util.LinkedHashSet<>(p.transactionCodes()));

        e.setTargetScope(p.target().scope());
        e.setTargetValue(p.target().value());

        e.setMode(p.mode());

        if (p.strategy() instanceof ElectronicRateTax rateTax) {
            e.setStrategyType(com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType.ELECTRONIC_RATE);
            e.setRate(rateTax.rate().value());
            e.setFixedAmount(null);
            e.setCurrency(Money.DEFAULT_CURRENCY);
        }

        if (p.strategy() instanceof FixedAmountTax fixedTax) {
            e.setStrategyType(com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType.FIXED_AMOUNT);
            e.setFixedAmount(fixedTax.fixed().amount());
            e.setRate(null);
            e.setCurrency(fixedTax.fixed().currency());
        }

        e.setFluxIntensity(p.rules().intensity().value());
        e.setExempted(p.rules().isExempted());

        e.setStatus(p.status());

        e.setBlockReason(p.blockReason());

        e.setCreatedBy(p.created().author());
        e.setCreatedAt(p.created().timestamp());

        if (p.lastModified() != null) {
            e.setLastModifiedBy(p.lastModified().author());
            e.setLastModifiedAt(p.lastModified().timestamp());
        }

        if (p.approvedOrRejected() != null) {
            e.setApprovedBy(p.approvedOrRejected().author());
            e.setApprovedAt(p.approvedOrRejected().timestamp());
        }

        return e;
    }

    public TaxPolicy toDomain(TaxPolicyEntity e) {

        TaxTarget target = switch (e.getTargetScope()) {
            case GLOBAL -> TaxTarget.global();
            case ACCOUNT_TYPE -> TaxTarget.accountType(e.getTargetValue());
            case ACCOUNT_ID -> TaxTarget.accountId(e.getTargetValue());
        };

        TaxStrategy strategy = switch (e.getStrategyType()) {

            case ELECTRONIC_RATE ->
                    new ElectronicRateTax(new TaxRate(e.getRate()));

            case FIXED_AMOUNT ->
                    new FixedAmountTax(Money.of(e.getFixedAmount(), e.getCurrency()));
        };

        TaxRules rules = new TaxRules(
                new FluxIntensity(e.getFluxIntensity()),
                e.isExempted()
        );

        AuditInfo created = new AuditInfo(
                e.getCreatedBy(),
                e.getCreatedAt(),
                "CREATED"
        );

        AuditInfo lastModified = e.getLastModifiedAt() == null
                ? null
                : new AuditInfo(
                e.getLastModifiedBy(),
                e.getLastModifiedAt(),
                "UPDATED"
        );

        AuditInfo approved = e.getApprovedAt() == null
                ? null
                : new AuditInfo(
                e.getApprovedBy(),
                e.getApprovedAt(),
                "APPROVED"
        );

        return TaxPolicy.reconstitute(
                TaxPolicyId.from(e.getId()),
                e.getTransactionCodes(),
                target,
                e.getMode(),
                strategy,
                rules,
                TaxPolicyStatus.valueOf(e.getStatus().name()),
                created,
                lastModified,
                approved,
                e.getBlockReason()
        );
    }
}