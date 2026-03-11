package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.fees.domain.compliance.FeePolicyComplianceData;
import com.kratos.mok.pricing.fees.domain.enums.FeePolicyStatus;
import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.fees.domain.enums.KycRequirement;
import com.kratos.mok.pricing.fees.domain.strategy.FeeRules;
import com.kratos.mok.pricing.fees.domain.strategy.FeeStrategy;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.InvalidStateException;
import com.kratos.mok.pricing.shared.domain.vo.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FeePolicy {

    private final FeePolicyId id;

    private TransactionCode transactionCode;
    private TransactionType transactionType;
    private FeeTarget target;

    private FeeStrategy strategy;
    private FeeRules rules;
    private KycRequirement kycRequirement;

    private ValidityPeriod validity;
    private Priority priority;

    private FeePolicyStatus status;
    private SuspensionWindow suspension;

    private AuditInfo created;
    private AuditInfo lastModified;
    private AuditInfo approvedOrRejected;
    private String blockReason;

    private FeePolicy(
            FeePolicyId id,
            TransactionCode transactionCode,
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeRules rules,
            KycRequirement kycRequirement,
            ValidityPeriod validity,
            Priority priority,
            FeePolicyStatus status,
            SuspensionWindow suspension,
            AuditInfo created,
            AuditInfo lastModified,
            AuditInfo approvedOrRejected
    ) {
        this.id = requireNonNull(id, "id");
        this.transactionCode = requireNonNull(transactionCode, "transactionCode");
        this.transactionType = requireNonNull(transactionType, "transactionType");
        this.target = requireNonNull(target, "target");
        this.strategy = requireNonNull(strategy, "strategy");
        this.rules = requireNonNull(rules, "rules");
        this.kycRequirement = (kycRequirement == null) ? KycRequirement.NONE : kycRequirement;
        this.validity = (validity == null) ? ValidityPeriod.permanent() : validity;
        this.priority = (priority == null) ? Priority.defaultFor(target.scope()) : priority;
        this.status = requireNonNull(status, "status");
        this.suspension = suspension;
        this.created = requireNonNull(created, "created");
        this.lastModified = lastModified;
        this.approvedOrRejected = approvedOrRejected;

        validateInvariants();
    }

    public static FeePolicy draft(
            TransactionCode transactionCode,
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeRules rules,
            KycRequirement kycRequirement,
            ValidityPeriod validity,
            Priority priority,
            String authorId,
            LocalDateTime when
    ) {
        var id = FeePolicyId.generate();
        var created = new AuditInfo(authorId, when, "DRAFT_CREATION");

        return new FeePolicy(
                id,
                transactionCode,
                transactionType,
                target,
                strategy,
                rules,
                kycRequirement,
                validity,
                priority,
                FeePolicyStatus.DRAFT,
                null,
                created,
                null,
                null
        );
    }

    public static FeePolicy reconstitute(
            FeePolicyId id,
            TransactionCode transactionCode,
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeRules rules,
            KycRequirement kycRequirement,
            ValidityPeriod validity,
            Priority priority,
            FeePolicyStatus status,
            SuspensionWindow suspension,
            AuditInfo created,
            AuditInfo lastModified,
            AuditInfo approvedOrRejected,
            String blockReason
    ) {
        var p = new FeePolicy(
                id,
                transactionCode,
                transactionType,
                target,
                strategy,
                rules,
                kycRequirement,
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

    public static FeePolicy bootstrapActive(
            TransactionCode transactionCode,
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeRules rules,
            KycRequirement kycRequirement,
            ValidityPeriod validity,
            Priority priority,
            String systemActor,
            LocalDateTime when
    ) {
        var id = FeePolicyId.generate();
        var audit = new AuditInfo(systemActor, when, "SYSTEM_BOOTSTRAP");

        return new FeePolicy(
                id,
                transactionCode,
                transactionType,
                target,
                strategy,
                rules,
                kycRequirement,
                validity,
                priority,
                FeePolicyStatus.ACTIVE,
                null,
                audit,
                audit,
                audit
        );
    }

    public void updatePolicy(
            TransactionCode newTransactionCode,
            TransactionType newTransactionType,
            FeeTarget newTarget,
            FeeStrategy newStrategy,
            FeeRules newRules,
            KycRequirement newKycRequirement,
            ValidityPeriod newValidity,
            Priority newPriority,
            String authorId,
            LocalDateTime when,
            String reason
    ) {
        ensureEditable();

        this.transactionCode = requireNonNull(newTransactionCode, "newTransactionCode");
        this.transactionType = requireNonNull(newTransactionType, "newTransactionType");
        this.target = requireNonNull(newTarget, "newTarget");
        this.strategy = requireNonNull(newStrategy, "newStrategy");
        this.rules = requireNonNull(newRules, "newRules");
        this.kycRequirement = (newKycRequirement == null) ? KycRequirement.NONE : newKycRequirement;
        this.validity = (newValidity == null) ? ValidityPeriod.permanent() : newValidity;
        this.priority = (newPriority == null) ? Priority.defaultFor(newTarget.scope()) : newPriority;

        this.status = FeePolicyStatus.PENDING_APPROVAL;
        this.suspension = null;
        this.lastModified = new AuditInfo(
                authorId,
                when,
                reason == null ? "UPDATE_AND_RESUBMIT" : reason
        );

        validateInvariants();
    }

    public void block(String code, String reason, String actor, LocalDateTime when) {
        if (code == null || code.isBlank()) {
            throw new DomainValidationException("BLOCK_CODE_REQUIRED", "block code is required", Map.of());
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainValidationException("BLOCK_REASON_REQUIRED", "block reason is required", Map.of());
        }
        if (status == FeePolicyStatus.ARCHIVED) {
            throw new InvalidStateException(
                    "INVALID_STATUS_TRANSITION",
                    "ARCHIVED policy cannot be blocked",
                    Map.of("currentStatus", status.name())
            );
        }

        this.status = FeePolicyStatus.BLOCKED;
        this.blockReason = code;
        this.lastModified = new AuditInfo(actor, when, reason);
    }

    public void submitForApproval(String authorId, LocalDateTime when, String reason) {
        if (status != FeePolicyStatus.DRAFT) {
            throw new InvalidStateException(
                    "INVALID_STATUS_TRANSITION",
                    "Only DRAFT policy can be submitted for approval",
                    Map.of("currentStatus", status.name(), "expectedStatus", FeePolicyStatus.DRAFT.name())
            );
        }
        this.status = FeePolicyStatus.PENDING_APPROVAL;
        this.lastModified = new AuditInfo(authorId, when, reason == null ? "SUBMIT_FOR_APPROVAL" : reason);
    }

    public void approve(String superAdminId, LocalDateTime when) {
        if (this.status != FeePolicyStatus.PENDING_APPROVAL) {
            throw new InvalidStateException(
                    "FEE_POLICY_NOT_APPROVABLE",
                    "Only PENDING_APPROVAL policies can be approved",
                    Map.of("currentStatus", this.status.name())
            );
        }
        this.status = FeePolicyStatus.ACTIVE;
        this.suspension = null;
        this.approvedOrRejected = new AuditInfo(superAdminId, when, "APPROVED");
        this.lastModified = this.approvedOrRejected;
    }

    public void reject(String superAdminId, LocalDateTime when, String justification) {
        if (status != FeePolicyStatus.PENDING_APPROVAL) {
            throw new InvalidStateException(
                    "INVALID_STATUS_TRANSITION",
                    "Only PENDING_APPROVAL policy can be rejected",
                    Map.of("currentStatus", status.name(), "expectedStatus", FeePolicyStatus.PENDING_APPROVAL.name())
            );
        }
        this.status = FeePolicyStatus.REJECTED;
        this.approvedOrRejected = new AuditInfo(superAdminId, when, justification == null ? "REJECTED" : justification);
        this.lastModified = this.approvedOrRejected;
    }

    public void suspend(LocalDateTime from, LocalDateTime to, String actorId, LocalDateTime when, String reason) {
        if (status != FeePolicyStatus.ACTIVE) {
            throw new InvalidStateException(
                    "INVALID_STATUS_TRANSITION",
                    "Only ACTIVE policy can be suspended",
                    Map.of("currentStatus", status.name(), "expectedStatus", FeePolicyStatus.ACTIVE.name())
            );
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new DomainValidationException(
                    "INVALID_SUSPENSION_PERIOD",
                    "suspensionFrom must be <= suspensionTo",
                    Map.of("from", from, "to", to)
            );
        }

        this.status = FeePolicyStatus.SUSPENDED;
        this.suspension = new SuspensionWindow(from, to);
        this.lastModified = new AuditInfo(actorId, when, reason == null ? "SUSPEND" : reason);
    }

    public void resume(String actorId, LocalDateTime when, String reason) {
        if (status != FeePolicyStatus.SUSPENDED) {
            throw new IllegalStateException("Only SUSPENDED policies can be resumed.");
        }
        this.status = FeePolicyStatus.ACTIVE;
        this.suspension = null;
        this.lastModified = new AuditInfo(actorId, when, reason == null ? "RESUME" : reason);
    }

    public void archive(String actorId, LocalDateTime when, String reason) {
        if (status == FeePolicyStatus.ARCHIVED) {
            return;
        }
        this.status = FeePolicyStatus.ARCHIVED;
        this.lastModified = new AuditInfo(actorId, when, reason == null ? "ARCHIVE" : reason);
    }

    public FeeComputation computeFee(Money transactionAmount, TransactionContext ctx, LocalDateTime now) {
        requireNonNull(transactionAmount, "transactionAmount");
        requireNonNull(ctx, "ctx");
        var at = (now == null) ? LocalDateTime.now() : now;

        ensureApplicable(at, ctx);

        if (rules.activationThreshold() != null && transactionAmount.compareTo(rules.activationThreshold()) <= 0) {
            return FeeComputation.free(
                    id,
                    transactionAmount,
                    strategy.type(),
                    "FREE_UNDER_THRESHOLD",
                    rules.activationThreshold(),
                    rules.minFee(),
                    rules.maxFee()
            );
        }

        Money raw = strategy.apply(transactionAmount);
        Money bounded = rules.applyMinMax(raw);

        return new FeeComputation(
                id,
                transactionAmount,
                bounded,
                strategy.type(),
                strategyRuleApplied(strategy.type()),
                rules.activationThreshold(),
                rules.minFee(),
                rules.maxFee()
        );
    }

    public boolean isApplicableAt(LocalDateTime at, TransactionContext ctx) {
        try {
            ensureApplicable(at, ctx);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void ensureApplicable(LocalDateTime at, TransactionContext ctx) {
        if (status != FeePolicyStatus.ACTIVE && status != FeePolicyStatus.SUSPENDED) {
            throw new IllegalStateException("FeePolicy not applicable (status=" + status + ")");
        }

        if (!validity.isValidAt(at)) {
            throw new IllegalStateException("FeePolicy not valid at " + at);
        }

        if (status == FeePolicyStatus.SUSPENDED) {
            if (suspension == null) {
                throw new IllegalStateException("Policy is SUSPENDED but suspension window is missing");
            }
            if (suspension.isActiveAt(at)) {
                throw new IllegalStateException("FeePolicy suspended until " + suspension.to());
            }
        }

        if (kycRequirement == KycRequirement.REQUIRED && !ctx.isKycValidated()) {
            throw new IllegalStateException("KYC is required but not verified");
        }

        rules.minMonthlyTxCountOpt().ifPresent(minTx -> {
            if (ctx.monthlyTransactionCount() < minTx) {
                throw new IllegalStateException("Monthly tx count requirement not met");
            }
        });
    }

    private void ensureEditable() {
        if (status == FeePolicyStatus.ARCHIVED) {
            throw new InvalidStateException(
                    "FEE_POLICY_NOT_EDITABLE",
                    "ARCHIVED policy is not editable",
                    Map.of("currentStatus", status.name())
            );
        }
    }

    private void validateInvariants() {
        if (transactionCode == null) {
            throw new IllegalArgumentException("transactionCode cannot be null");
        }
        if (target.scope() == null) {
            throw new IllegalArgumentException("target.scope cannot be null");
        }
        if (target.value() == null || target.value().isBlank()) {
            throw new IllegalArgumentException("target.value cannot be null/blank");
        }
        if (priority.value() < 0) {
            throw new IllegalArgumentException("priority cannot be negative");
        }
    }

    private static String strategyRuleApplied(FeeStrategyType type) {
        return switch (type) {
            case FIXED -> "FIXED";
            case PROPORTIONAL -> "PERCENTAGE";
            case TIERED -> "TIERED";
        };
    }

    private static <T> T requireNonNull(T value, String name) {
        return Objects.requireNonNull(value, name + " must not be null");
    }

    public FeePolicyId id() { return id; }
    public TransactionCode transactionCode() { return transactionCode; }
    public TransactionType transactionType() { return transactionType; }
    public FeeTarget target() { return target; }
    public FeeStrategy strategy() { return strategy; }
    public FeeRules rules() { return rules; }
    public KycRequirement kycRequirement() { return kycRequirement; }
    public ValidityPeriod validity() { return validity; }
    public Priority priority() { return priority; }
    public FeePolicyStatus status() { return status; }
    public Optional<SuspensionWindow> suspension() { return Optional.ofNullable(suspension); }
    public AuditInfo created() { return created; }
    public Optional<AuditInfo> lastModified() { return Optional.ofNullable(lastModified); }
    public Optional<AuditInfo> approvedOrRejected() { return Optional.ofNullable(approvedOrRejected); }
    public String blockReason() { return blockReason; }

    public FeePolicyComplianceData toComplianceData() {
        return new FeePolicyComplianceData(
                this.transactionType,
                this.target,
                this.strategy.type(),
                this.rules.minFee(),
                this.rules.maxFee(),
                this.rules.activationThreshold(),
                this.kycRequirement == KycRequirement.REQUIRED
        );
    }
}