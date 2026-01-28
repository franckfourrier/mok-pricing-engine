package com.kratos.mok.pricing.fees.infrastructure.mapper;

import com.kratos.mok.pricing.fees.domain.*;
import com.kratos.mok.pricing.fees.domain.enums.*;
import com.kratos.mok.pricing.fees.domain.strategy.FeeRules;
import com.kratos.mok.pricing.fees.domain.strategy.FeeStrategy;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.fees.domain.vo.PolicyPriority;
import com.kratos.mok.pricing.fees.infrastructure.model.AuditEmbeddable;
import com.kratos.mok.pricing.fees.infrastructure.model.FeePolicyEntity;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class FeePolicyEntityMapper {

    public static FeePolicyEntity fromDomain(FeePolicy p) {
        if (p == null) return null;

        FeePolicyEntity e = new FeePolicyEntity();

        e.setId(p.id().value());
        e.setTransactionType(p.transactionType());

        e.setTargetScope(p.target().scope().name());
        e.setTargetValue(p.target().value());

        e.setPriority(p.priority().value());

        e.setStrategy(p.strategy());

        // Rules
        e.setActivationThreshold(p.rules().activationThreshold() == null ? null : p.rules().activationThreshold().amount());
        e.setMinFee(p.rules().minFee() == null ? null : p.rules().minFee().amount());
        e.setMaxFee(p.rules().maxFee() == null ? null : p.rules().maxFee().amount());
        //e.setMinMonthlyTxCount(p.rules().minMonthlyTxCount().orElse(null));

        // Validity
        ValidityPeriod v = p.validity();
        e.setValidityStart(v.start());
        e.setValidityEnd(v.end());

        // KYC (enum -> boolean)
        e.setKycRequired(p.kycRequirement() == KycRequirement.REQUIRED);

        // Lifecycle
        e.setStatus(p.status().name());

        // Block reason
        // (nécessite le getter p.blockReason() comme proposé)
        if (p.status() == FeePolicyStatus.BLOCKED) {
            //e.setBlockReason(p.blockReason());
        } else {
            e.setBlockReason(null);
        }

        // Suspension
        SuspensionWindow sw = p.suspension().orElse(null);
        if (sw != null) {
            e.setSuspensionFrom(sw.from());
            e.setSuspensionTo(sw.to());
        } else {
            e.setSuspensionFrom(null);
            e.setSuspensionTo(null);
        }

        // Audit
        e.setCreatedBy(AuditEmbeddable.fromDomain(p.created()));
        e.setLastModifiedBy(AuditEmbeddable.fromDomain(p.lastModified().orElse(null)));
        e.setApprovedOrRejectedBy(AuditEmbeddable.fromDomain(p.approvedOrRejected().orElse(null)));

        return e;
    }
    public FeePolicy toDomain(FeePolicyEntity e) {
        if (e == null) return null;

        FeePolicyId id = FeePolicyId.from(e.getId());

        TransactionType txType = e.getTransactionType();
        TargetScope scope = TargetScope.valueOf(e.getTargetScope());
        FeeTarget target = new FeeTarget(scope, e.getTargetValue());

        PolicyPriority priority = PolicyPriority.of(e.getPriority());

        // Strategy JSON -> déjà matérialisée si Hibernate JSON le fait.
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
        if (e.getSuspensionFrom() != null && e.getSuspensionTo() != null) {
            suspension = new SuspensionWindow(e.getSuspensionFrom(), e.getSuspensionTo());
        }

        AuditInfo created = e.getCreatedBy() == null ? null : e.getCreatedBy().toDomain();
        AuditInfo lastModified = e.getLastModifiedBy() == null ? null : e.getLastModifiedBy().toDomain();
        AuditInfo approvedOrRejected = e.getApprovedOrRejectedBy() == null ? null : e.getApprovedOrRejectedBy().toDomain();

        return FeePolicy.reconstitute(
                id,
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
