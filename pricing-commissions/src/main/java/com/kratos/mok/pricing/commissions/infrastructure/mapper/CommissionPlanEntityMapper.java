package com.kratos.mok.pricing.commissions.infrastructure.mapper;

import com.kratos.mok.pricing.commissions.domain.CommissionPlan;
import com.kratos.mok.pricing.commissions.domain.CommissionTarget;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionPlanId;
import com.kratos.mok.pricing.commissions.infrastructure.model.CommissionPlanEntity;
import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import com.kratos.mok.pricing.shared.domain.vo.Priority;
import com.kratos.mok.pricing.shared.domain.vo.SuspensionWindow;
import com.kratos.mok.pricing.shared.domain.vo.ValidityPeriod;
import com.kratos.mok.pricing.shared.infrastructure.config.model.AuditEmbeddable;
import org.springframework.stereotype.Component;

@Component
public class CommissionPlanEntityMapper {

    public CommissionPlan toDomain(CommissionPlanEntity e) {
        if (e == null) return null;

        var validity = (e.getValidityStart() == null && e.getValidityEnd() == null)
                ? ValidityPeriod.permanent()
                : new ValidityPeriod(e.getValidityStart(), e.getValidityEnd());

        var suspension = (e.getSuspensionFrom() == null && e.getSuspensionTo() == null)
                ? null
                : new SuspensionWindow(e.getSuspensionFrom(), e.getSuspensionTo());

        var created = toAuditInfo(e.getCreatedBy());
        var modified = toAuditInfoNullable(e.getLastModifiedBy());
        var approved = toAuditInfoNullable(e.getApprovedOrRejectedBy());

        return CommissionPlan.reconstitute(
                CommissionPlanId.from(e.getId()),
                e.getTransactionType(),
                new CommissionTarget(e.getTargetScope(), e.getTargetValue()),
                e.getStrategy(),
                validity,
                Priority.of(e.getPriority()),
                e.getStatus(),
                suspension,
                created,
                modified,
                approved,
                e.getBlockReason()
        );
    }

    public CommissionPlanEntity toEntity(CommissionPlan d) {
        if (d == null) return null;

        var e = new CommissionPlanEntity();

        e.setId(d.id().value());
        e.setTransactionType(d.transactionType());
        e.setTargetScope(d.target().scope());
        e.setTargetValue(d.target().value());
        e.setPriority(d.priority().value());
        e.setStatus(d.status());

        // validity
        e.setValidityStart(d.validity().start());
        e.setValidityEnd(d.validity().end());

        // suspension
        e.setSuspensionFrom(d.suspension().map(SuspensionWindow::from).orElse(null));
        e.setSuspensionTo(d.suspension().map(SuspensionWindow::to).orElse(null));

        // strategy (jsonb)
        e.setStrategy(d.strategy());

        // audit
        e.setCreatedBy(toEmbeddable(d.created()));
        d.lastModified().ifPresent(a -> e.setLastModifiedBy(toEmbeddable(a)));
        d.approvedOrRejected().ifPresent(a -> e.setApprovedOrRejectedBy(toEmbeddable(a)));

        e.setBlockReason(d.blockReason());

        return e;
    }

    private static AuditEmbeddable toEmbeddable(AuditInfo a) {
        if (a == null) return null;
        return new AuditEmbeddable(a.author(), a.timestamp(), a.reason());
    }

    private static AuditInfo toAuditInfo(AuditEmbeddable a) {
        if (a == null) throw new IllegalStateException("created audit is required");
        return new AuditInfo(a.getAuthor(), a.getTimestamp(), a.getReason());
    }

    private static AuditInfo toAuditInfoNullable(AuditEmbeddable a) {
        if (a == null) return null;
        return new AuditInfo(a.getAuthor(), a.getTimestamp(), a.getReason());
    }
}
