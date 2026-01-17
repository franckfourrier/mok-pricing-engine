package com.kratos.mok.pricing.fees.infrastructure.mapper;

import com.kratos.mok.pricing.fees.domain.FeeLimits;
import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.FeeTarget;
import com.kratos.mok.pricing.fees.domain.ValidityPeriod;
import com.kratos.mok.pricing.fees.domain.enums.FeePolicyStatus;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.snapshot.FeePolicySnapshot;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.fees.infrastructure.model.AuditEmbeddable;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FeePolicyMapper {

    public FeePolicyEntity toEntity(FeePolicy domain) {

        FeePolicySnapshot snapshot = domain.snapshot();

        FeePolicyEntity entity = new FeePolicyEntity();

        entity.setId(snapshot.id());
        entity.setTransactionType(TransactionType.valueOf(snapshot.transactionType()));
        entity.setTargetScope(snapshot.targetScope());
        entity.setTargetValue(snapshot.targetValue());
        entity.setPriority(domain.priority());

        entity.setStrategy(domain.strategy());

        if (snapshot.limitsMin() != null) entity.setLimitMin(new BigDecimal(snapshot.limitsMin()));
        if (snapshot.limitsMax() != null) entity.setLimitMax(new BigDecimal(snapshot.limitsMax()));

        entity.setActivationThreshold(new BigDecimal(snapshot.activationThreshold()));

        entity.setKycRequired(snapshot.kycRequired());
        entity.setStatus(snapshot.status());

        entity.setCreatedBy(new AuditEmbeddable(
                snapshot.createdBy(),
                snapshot.createdDate(),
                "Initial creation"
        ));

        if (snapshot.lastModifiedBy() != null) {
            entity.setLastModifiedBy(new AuditEmbeddable(
                    snapshot.lastModifiedBy(),
                    snapshot.lastModifiedDate(),
                    "Modification"
            ));
        }

        return entity;
    }

    public FeePolicy toDomain(FeePolicyEntity entity) {
        FeePolicyId id = FeePolicyId.from(entity.getId());

        FeeTarget target = new FeeTarget(
                FeeTarget.Scope.valueOf(entity.getTargetScope()),
                entity.getTargetValue()
        );

        FeeLimits limits = new FeeLimits(
                entity.getLimitMin() != null ? new Money(entity.getLimitMin()) : null,
                entity.getLimitMax() != null ? new Money(entity.getLimitMax()) : null
        );

        Money activationThreshold = entity.getActivationThreshold() != null
                ? new Money(entity.getActivationThreshold())
                : Money.ZERO;

        ValidityPeriod validity = new ValidityPeriod(
                entity.getValidityStart(),
                entity.getValidityEnd()
        );

        AuditInfo createdBy = new AuditInfo(
                entity.getCreatedBy().getAuthor(),
                entity.getCreatedBy().getTimestamp(),
                entity.getCreatedBy().getReason()
        );

        AuditInfo validatedBy = null;
        if (entity.getLastModifiedBy() != null) {
            validatedBy = new AuditInfo(
                    entity.getLastModifiedBy().getAuthor(),
                    entity.getLastModifiedBy().getTimestamp(),
                    entity.getLastModifiedBy().getReason()
            );
        }


        return FeePolicy.reconstitute(
                id,
                entity.getTransactionType(),
                target,
                entity.getStrategy(),
                limits,
                activationThreshold,
                validity,
                entity.isKycRequired(),
                FeePolicyStatus.valueOf(entity.getStatus()),
                createdBy,
                validatedBy
        );
    }
}
