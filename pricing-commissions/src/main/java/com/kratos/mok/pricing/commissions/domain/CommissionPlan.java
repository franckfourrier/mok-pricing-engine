package com.kratos.mok.pricing.commissions.domain;

import com.kratos.mok.pricing.commissions.domain.compliance.CommissionPlanComplianceData;
import com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus;
import com.kratos.mok.pricing.commissions.domain.policy.CommissionPlanValidator;
import com.kratos.mok.pricing.commissions.domain.strategy.CommissionStrategy;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionPlanId;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.InvalidStateException;
import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import com.kratos.mok.pricing.shared.domain.vo.Priority;
import com.kratos.mok.pricing.shared.domain.vo.SuspensionWindow;
import com.kratos.mok.pricing.shared.domain.vo.ValidityPeriod;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CommissionPlan {

    private final CommissionPlanId id;

    private final TransactionType transactionType;
    private final CommissionTarget target;

    private CommissionStrategy strategy;

    private ValidityPeriod validity;         // start/end optionnel
    private Priority priority;         // tie-breaker

    private CommissionPlanStatus status;     // DRAFT, PENDING_APPROVAL, ACTIVE, ...
    private SuspensionWindow suspension;     // optionnel

    private AuditInfo created;
    private AuditInfo lastModified;
    private AuditInfo approvedOrRejected;
    private String blockReason;

    private CommissionPlan(
            CommissionPlanId id,
            TransactionType transactionType,
            CommissionTarget target,
            CommissionStrategy strategy,
            ValidityPeriod validity,
            Priority priority,
            CommissionPlanStatus status,
            SuspensionWindow suspension,
            AuditInfo created,
            AuditInfo lastModified,
            AuditInfo approvedOrRejected
    ) {
        this.id = requireNonNull(id, "id");
        this.transactionType = requireNonNull(transactionType, "transactionType");
        this.target = requireNonNull(target, "target");
        this.strategy = requireNonNull(strategy, "strategy");
        this.validity = (validity == null) ? ValidityPeriod.permanent() : validity;
        this.priority = (priority == null) ? Priority.defaultFor(target.scope()) : priority;
        this.status = requireNonNull(status, "status");
        this.suspension = suspension; // nullable
        this.created = requireNonNull(created, "created");
        this.lastModified = lastModified;
        this.approvedOrRejected = approvedOrRejected;

        validateInvariants();
    }

    // -------------------------
    // Factories
    // -------------------------

    public static CommissionPlan draft(
            TransactionType transactionType,
            CommissionTarget target,
            CommissionStrategy strategy,
            ValidityPeriod validity,
            Priority priority,
            String authorId,
            LocalDateTime when
    ) {
        var id = CommissionPlanId.generate();
        var created = new AuditInfo(authorId, when, "DRAFT_CREATION");

        return new CommissionPlan(
                id,
                transactionType,
                target,
                strategy,
                validity,
                priority,
                CommissionPlanStatus.DRAFT,
                null,
                created,
                null,
                null
        );
    }

    /**
     * Reconstitution depuis la persistence.
     * (On vérifie les invariants, pas les transitions.)
     */
    public static CommissionPlan reconstitute(
            CommissionPlanId id,
            TransactionType transactionType,
            CommissionTarget target,
            CommissionStrategy strategy,
            ValidityPeriod validity,
            Priority priority,
            CommissionPlanStatus status,
            SuspensionWindow suspension,
            AuditInfo created,
            AuditInfo lastModified,
            AuditInfo approvedOrRejected,
            String blockReason
    ) {
        var p = new CommissionPlan(
                id,
                transactionType,
                target,
                strategy,
                validity,
                priority,
                status,
                suspension,
                created,
                lastModified,
                approvedOrRejected
        );
        p.blockReason = blockReason;
        return p;
    }

    // -------------------------
    // Commands (business methods)
    // -------------------------

    public void updateConfiguration(
            CommissionStrategy newStrategy,
            ValidityPeriod newValidity,
            Priority newPriority,
            String authorId,
            LocalDateTime when,
            String reason
    ) {
        ensureEditable();

        this.strategy = requireNonNull(newStrategy, "newStrategy");
        this.validity = (newValidity == null) ? ValidityPeriod.permanent() : newValidity;
        this.priority = (newPriority == null) ? Priority.defaultFor(target.scope()) : newPriority;
        this.lastModified = new AuditInfo(authorId, when, reason == null ? "UPDATE" : reason);

        validateInvariants();
    }

    public void submitForApproval(String authorId, LocalDateTime when, String reason) {
        if (status != CommissionPlanStatus.DRAFT) {
            throw new InvalidStateException(
                    "INVALID_STATUS_TRANSITION",
                    "Only DRAFT plan can be submitted for approval",
                    Map.of("currentStatus", status.name(), "expectedStatus", CommissionPlanStatus.DRAFT.name())
            );
        }
        this.status = CommissionPlanStatus.PENDING_APPROVAL;
        this.lastModified = new AuditInfo(authorId, when, reason == null ? "SUBMIT_FOR_APPROVAL" : reason);
    }

    public void approve(String superAdminId, LocalDateTime when, String justification) {
        if (status != CommissionPlanStatus.PENDING_APPROVAL) {
            throw new InvalidStateException(
                    "COMMISSION_PLAN_NOT_APPROVABLE",
                    "Only PENDING_APPROVAL plans can be approved",
                    Map.of("currentStatus", status.name())
            );
        }
        this.status = CommissionPlanStatus.ACTIVE;
        this.suspension = null;
        this.approvedOrRejected = new AuditInfo(superAdminId, when, justification == null ? "APPROVED" : justification);
        this.lastModified = this.approvedOrRejected;
    }

    public void reject(String superAdminId, LocalDateTime when, String justification) {
        if (status != CommissionPlanStatus.PENDING_APPROVAL) {
            throw new InvalidStateException(
                    "INVALID_STATUS_TRANSITION",
                    "Only PENDING_APPROVAL plan can be rejected",
                    Map.of("currentStatus", status.name(), "expectedStatus", CommissionPlanStatus.PENDING_APPROVAL.name())
            );
        }
        this.status = CommissionPlanStatus.REJECTED;
        this.approvedOrRejected = new AuditInfo(superAdminId, when, justification == null ? "REJECTED" : justification);
        this.lastModified = this.approvedOrRejected;
    }

    public void suspend(LocalDateTime from, LocalDateTime to, String actorId, LocalDateTime when, String reason) {
        if (status != CommissionPlanStatus.ACTIVE) {
            throw new InvalidStateException(
                    "INVALID_STATUS_TRANSITION",
                    "Only ACTIVE plan can be suspended",
                    Map.of("currentStatus", status.name(), "expectedStatus", CommissionPlanStatus.ACTIVE.name())
            );
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new DomainValidationException(
                    "INVALID_SUSPENSION_PERIOD",
                    "suspensionFrom must be <= suspensionTo",
                    Map.of("from", from, "to", to)
            );
        }
        this.status = CommissionPlanStatus.SUSPENDED;
        this.suspension = new SuspensionWindow(from, to);
        this.lastModified = new AuditInfo(actorId, when, reason == null ? "SUSPEND" : reason);
    }

    public void resume(String actorId, LocalDateTime when, String reason) {
        if (status != CommissionPlanStatus.SUSPENDED) {
            throw new InvalidStateException(
                    "INVALID_STATUS_TRANSITION",
                    "Only SUSPENDED plan can be resumed",
                    Map.of("currentStatus", status.name(), "expectedStatus", CommissionPlanStatus.SUSPENDED.name())
            );
        }
        this.status = CommissionPlanStatus.ACTIVE;
        this.suspension = null;
        this.lastModified = new AuditInfo(actorId, when, reason == null ? "RESUME" : reason);
    }

    public void archive(String actorId, LocalDateTime when, String reason) {
        if (status == CommissionPlanStatus.ARCHIVED) return;
        this.status = CommissionPlanStatus.ARCHIVED;
        this.lastModified = new AuditInfo(actorId, when, reason == null ? "ARCHIVE" : reason);
    }

    public void block(String code, String reason, String actorId, LocalDateTime when) {
        if (code == null || code.isBlank()) {
            throw new DomainValidationException("BLOCK_CODE_REQUIRED", "block code is required", Map.of());
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainValidationException("BLOCK_REASON_REQUIRED", "block reason is required", Map.of());
        }
        if (status == CommissionPlanStatus.ARCHIVED) {
            throw new InvalidStateException(
                    "INVALID_STATUS_TRANSITION",
                    "ARCHIVED plan cannot be blocked",
                    Map.of("currentStatus", status.name())
            );
        }
        this.status = CommissionPlanStatus.BLOCKED;
        this.blockReason = code;
        this.lastModified = new AuditInfo(actorId, when, reason);
    }

    public CommissionPlanComplianceData toComplianceData() {
        return new CommissionPlanComplianceData(
                this.transactionType,
                this.target,
                this.strategy.type(),
                CommissionSharesExtractor.extract(this.strategy)
        );
    }

    // -------------------------
    // Query helpers (pure domain)
    // -------------------------

    public boolean isApplicableAt(LocalDateTime at) {
        try {
            ensureApplicable(at);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void ensureApplicable(LocalDateTime at) {
        var when = (at == null) ? LocalDateTime.now() : at;

        if (status != CommissionPlanStatus.ACTIVE && status != CommissionPlanStatus.SUSPENDED) {
            throw new IllegalStateException("CommissionPlan not applicable (status=" + status + ")");
        }

        if (!validity.isValidAt(when)) {
            throw new IllegalStateException("CommissionPlan not valid at " + when);
        }

        if (status == CommissionPlanStatus.SUSPENDED) {
            if (suspension == null) {
                throw new IllegalStateException("Plan is SUSPENDED but suspension window is missing");
            }
            if (suspension.isActiveAt(when)) {
                throw new IllegalStateException("CommissionPlan suspended until " + suspension.to());
            }
        }
    }

    private void ensureEditable() {
        if (status != CommissionPlanStatus.DRAFT) {
            throw new InvalidStateException(
                    "PLAN_NOT_EDITABLE",
                    "Only DRAFT plan is editable",
                    Map.of("currentStatus", status.name(), "expectedStatus", CommissionPlanStatus.DRAFT.name())
            );
        }
    }

    private void validateInvariants() {
        if (target.scope() == null) {
            throw new IllegalArgumentException("target.scope cannot be null");
        }
        if (target.value() == null || target.value().isBlank()) {
            throw new IllegalArgumentException("target.value cannot be null/blank");
        }
        if (priority.value() < 0) {
            throw new IllegalArgumentException("priority cannot be negative");
        }

        // validations métier (somme=100%, etc.)
        CommissionPlanValidator.validate(strategy);
    }

    private static <T> T requireNonNull(T value, String name) {
        return Objects.requireNonNull(value, name + " must not be null");
    }

    // -------------------------
    // Getters
    // -------------------------

    public CommissionPlanId id() { return id; }

    public TransactionType transactionType() { return transactionType; }

    public CommissionTarget target() { return target; }

    public CommissionStrategy strategy() { return strategy; }

    public ValidityPeriod validity() { return validity; }

    public Priority priority() { return priority; }

    public CommissionPlanStatus status() { return status; }

    public Optional<SuspensionWindow> suspension() { return Optional.ofNullable(suspension); }

    public AuditInfo created() { return created; }

    public Optional<AuditInfo> lastModified() { return Optional.ofNullable(lastModified); }

    public Optional<AuditInfo> approvedOrRejected() { return Optional.ofNullable(approvedOrRejected); }

    public String blockReason() { return blockReason; }
}
