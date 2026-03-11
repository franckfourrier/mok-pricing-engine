package com.kratos.mok.pricing.fees.infrastructure.mapper;

import com.kratos.mok.pricing.fees.domain.*;
import com.kratos.mok.pricing.fees.domain.enums.FeePolicyStatus;
import com.kratos.mok.pricing.fees.domain.enums.KycRequirement;
import com.kratos.mok.pricing.fees.domain.strategy.FeeRules;
import com.kratos.mok.pricing.fees.domain.strategy.FeeStrategy;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.vo.*;
import com.kratos.mok.pricing.shared.infrastructure.config.model.AuditEmbeddable;
import org.springframework.stereotype.Component;

@Component
public class FeePolicyEntityMapper {

    public FeePolicyEntity toEntity(FeePolicy p) {
        if (p == null) return null;

        FeePolicyEntity e = new FeePolicyEntity();

        e.setId(p.id().value());
        e.setTransactionCode(p.transactionCode());
        e.setTransactionType(p.transactionType());

        e.setTargetScope(p.target().scope());
        e.setTargetValue(p.target().value());

        e.setPriority(p.priority().value());
        e.setStrategy(p.strategy());

        e.setActivationThreshold(p.rules().activationThreshold() == null ? null : p.rules().activationThreshold().amount());
        e.setMinFee(p.rules().minFee() == null ? null : p.rules().minFee().amount());
        e.setMaxFee(p.rules().maxFee() == null ? null : p.rules().maxFee().amount());
        e.setMinMonthlyTxCount(p.rules().minMonthlyTxCount());

        ValidityPeriod v = p.validity();
        e.setValidityStart(v.start());
        e.setValidityEnd(v.end());

        e.setKycRequired(p.kycRequirement() == KycRequirement.REQUIRED);
        e.setStatus(p.status().name());
        e.setBlockReason(p.blockReason());

        SuspensionWindow sw = p.suspension().orElse(null);
        if (sw != null) {
            e.setSuspensionFrom(sw.from());
            e.setSuspensionTo(sw.to());
        } else {
            e.setSuspensionFrom(null);
            e.setSuspensionTo(null);
        }

        e.setCreatedBy(AuditEmbeddable.fromDomain(p.created()));
        e.setLastModifiedBy(AuditEmbeddable.fromDomain(p.lastModified().orElse(null)));
        e.setApprovedOrRejectedBy(AuditEmbeddable.fromDomain(p.approvedOrRejected().orElse(null)));

        return e;
    }

    public FeePolicy toDomain(FeePolicyEntity e) {
        if (e == null) return null;

        FeePolicyId id = FeePolicyId.from(e.getId());

        TransactionCode transactionCode = e.getTransactionCode();
        TransactionType txType = e.getTransactionType();
        TargetScope scope = e.getTargetScope();
        FeeTarget target = new FeeTarget(scope, e.getTargetValue());

        Priority priority = Priority.of(e.getPriority());
        FeeStrategy strategy = e.getStrategy();

        Money threshold = e.getActivationThreshold() == null ? null : Money.of(e.getActivationThreshold());
        Money minFee = e.getMinFee() == null ? null : Money.of(e.getMinFee());
        Money maxFee = e.getMaxFee() == null ? null : Money.of(e.getMaxFee());

        FeeRules rules = new FeeRules(
                threshold,
                minFee,
                maxFee,
                e.getMinMonthlyTxCount()
        );

        ValidityPeriod validity = (e.getValidityStart() == null && e.getValidityEnd() == null)
                ? ValidityPeriod.PERMANENT
                : new ValidityPeriod(e.getValidityStart(), e.getValidityEnd());

        KycRequirement kyc = e.isKycRequired() ? KycRequirement.REQUIRED : KycRequirement.NONE;
        FeePolicyStatus status = FeePolicyStatus.valueOf(e.getStatus());

        SuspensionWindow suspension = null;
        if (e.getSuspensionFrom() != null || e.getSuspensionTo() != null) {
            suspension = new SuspensionWindow(e.getSuspensionFrom(), e.getSuspensionTo());
        }

        AuditInfo created = e.getCreatedBy() == null ? null : e.getCreatedBy().toDomain();
        AuditInfo lastModified = e.getLastModifiedBy() == null ? null : e.getLastModifiedBy().toDomain();
        AuditInfo approvedOrRejected = e.getApprovedOrRejectedBy() == null ? null : e.getApprovedOrRejectedBy().toDomain();

        return FeePolicy.reconstitute(
                id,
                transactionCode,
                txType,
                target,
                strategy,
                rules,
                kyc,
                validity,
                priority,
                status,
                suspension,
                created,
                lastModified,
                approvedOrRejected,
                e.getBlockReason()
        );
    }
}
