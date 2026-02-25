package com.kratos.mok.pricing.taxes.domain;

import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.InvalidStateException;
import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxPolicyStatus;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxRules;
import com.kratos.mok.pricing.taxes.domain.strategy.TaxStrategy;
import com.kratos.mok.pricing.taxes.domain.vo.TaxPolicyId;
import com.kratos.mok.pricing.taxes.domain.vo.FluxIntensity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class TaxPolicy {

    private final TaxPolicyId id;

    private final TransactionType transactionType;
    private final TaxTarget target;

    private TaxMode mode;
    private TaxStrategy strategy;
    private TaxRules rules;

    private TaxPolicyStatus status;

    private AuditInfo created;
    private AuditInfo lastModified;
    private AuditInfo approvedOrRejected;

    private String blockReason;

    private TaxPolicy(
            TaxPolicyId id,
            TransactionType transactionType,
            TaxTarget target,
            TaxMode mode,
            TaxStrategy strategy,
            TaxRules rules,
            TaxPolicyStatus status,
            AuditInfo created,
            AuditInfo lastModified,
            AuditInfo approvedOrRejected,
            String blockReason
    ) {
        this.id = requireNonNull(id, "id");
        this.transactionType = requireNonNull(transactionType, "transactionType");
        this.target = requireNonNull(target, "target");
        this.mode = requireNonNull(mode, "mode");
        this.strategy = requireNonNull(strategy, "strategy");
        this.rules = (rules == null) ? TaxRules.standard() : rules;
        this.status = requireNonNull(status, "status");
        this.created = requireNonNull(created, "created");
        this.lastModified = lastModified;
        this.approvedOrRejected = approvedOrRejected;
        this.blockReason = blockReason;

        validateInvariants();
    }

    // -------------------------
    // Factories
    // -------------------------

    public static TaxPolicy draft(
            TransactionType transactionType,
            TaxTarget target,
            TaxMode mode,
            TaxStrategy strategy,
            TaxRules rules,
            String author,
            LocalDateTime when
    ) {
        var created = new AuditInfo(author, when, "DRAFT_CREATION");
        return new TaxPolicy(
                TaxPolicyId.generate(),
                transactionType,
                target,
                mode,
                strategy,
                rules,
                TaxPolicyStatus.DRAFT,
                created,
                null,
                null,
                null
        );
    }

    public static TaxPolicy reconstitute(
            TaxPolicyId id,
            TransactionType transactionType,
            TaxTarget target,
            TaxMode mode,
            TaxStrategy strategy,
            TaxRules rules,
            TaxPolicyStatus status,
            AuditInfo created,
            AuditInfo lastModified,
            AuditInfo approvedOrRejected,
            String blockReason
    ) {
        return new TaxPolicy(
                id, transactionType, target, mode, strategy, rules, status,
                created, lastModified, approvedOrRejected, blockReason
        );
    }



    // -------------------------
    // Commands (business methods)
    // -------------------------

    public void submitForApproval(String actor, LocalDateTime at, String reason) {
        if (status != TaxPolicyStatus.DRAFT) {
            throw new InvalidStateException(
                    "TAX_POLICY_NOT_SUBMITTABLE",
                    "Only DRAFT policies can be submitted",
                    Map.of("currentStatus", status.name())
            );
        }
        this.status = TaxPolicyStatus.PENDING_APPROVAL;
        this.lastModified = new AuditInfo(actor, at, reason == null ? "SUBMIT_FOR_APPROVAL" : reason);
    }

    public void approve(String superAdmin, LocalDateTime at, String justification) {
        if (status != TaxPolicyStatus.PENDING_APPROVAL) {
            throw new InvalidStateException(
                    "TAX_POLICY_NOT_APPROVABLE",
                    "Only PENDING_APPROVAL policies can be approved",
                    Map.of("currentStatus", status.name())
            );
        }
        this.status = TaxPolicyStatus.ACTIVE;
        this.approvedOrRejected = new AuditInfo(superAdmin, at, justification == null ? "APPROVED" : justification);
        this.lastModified = this.approvedOrRejected;
    }

    public void suspend(String actor, LocalDateTime at, String reason) {
        if (status != TaxPolicyStatus.ACTIVE) {
            throw new InvalidStateException(
                    "TAX_POLICY_NOT_SUSPENDABLE",
                    "Only ACTIVE policies can be suspended",
                    Map.of("currentStatus", status.name())
            );
        }

        this.status = TaxPolicyStatus.SUSPENDED;
        this.lastModified = new AuditInfo(actor, at, reason == null ? "SUSPENDED" : reason);
    }

    public void resume(String actor, LocalDateTime at, String reason) {
        if (status != TaxPolicyStatus.SUSPENDED) {
            throw new InvalidStateException(
                    "TAX_POLICY_NOT_RESUMABLE",
                    "Only SUSPENDED policies can be resumed",
                    Map.of("currentStatus", status.name())
            );
        }
        this.status = TaxPolicyStatus.ACTIVE;
        this.lastModified = new AuditInfo(actor, at, reason == null ? "RESUMED" : reason);
    }



    public void reject(String superAdmin, LocalDateTime at, String justification) {
        if (status != TaxPolicyStatus.PENDING_APPROVAL) {
            throw new InvalidStateException(
                    "TAX_POLICY_NOT_REJECTABLE",
                    "Only PENDING_APPROVAL policies can be rejected",
                    Map.of("currentStatus", status.name())
            );
        }
        this.status = TaxPolicyStatus.REJECTED;
        this.approvedOrRejected = new AuditInfo(superAdmin, at, justification == null ? "REJECTED" : justification);
        this.lastModified = this.approvedOrRejected;
    }

    public void block(String code, String reason, String actor, LocalDateTime at) {
        if (code == null || code.isBlank()) {
            throw new DomainValidationException("BLOCK_CODE_REQUIRED", "Block code is required", Map.of());
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainValidationException("BLOCK_REASON_REQUIRED", "Block reason is required", Map.of());
        }
        this.status = TaxPolicyStatus.BLOCKED;
        this.blockReason = code;
        this.lastModified = new AuditInfo(actor, at, reason);
    }

    public void archive(String actor, LocalDateTime at, String reason) {
        if (status == TaxPolicyStatus.ARCHIVED) return;
        this.status = TaxPolicyStatus.ARCHIVED;
        this.lastModified = new AuditInfo(actor, at, reason == null ? "ARCHIVE" : reason);
    }

    public void updateConfiguration(
            TaxMode mode,
            TaxStrategy strategy,
            TaxRules rules,
            String actor,
            LocalDateTime at,
            String reason
    ) {
        ensureEditable();

        this.mode = requireNonNull(mode, "mode");
        this.strategy = requireNonNull(strategy, "strategy");
        this.rules = (rules == null) ? TaxRules.standard() : rules;

        this.lastModified = new AuditInfo(actor, at, reason == null ? "UPDATE" : reason);
        validateInvariants();
    }

    // -------------------------
    // Query (pure domain)
    // -------------------------

    public Money computeTax(Money baseAmount) {
        Objects.requireNonNull(baseAmount, "baseAmount must not be null");

        if (this.status != TaxPolicyStatus.ACTIVE) {
            throw new InvalidStateException(
                    "TAX_POLICY_NOT_ACTIVE",
                    "TaxPolicy must be ACTIVE to compute tax",
                    Map.of("currentStatus", status.name())
            );
        }

        if (rules.isExempted()) {
            return Money.of(BigDecimal.ZERO, baseAmount.currency());
        }

        FluxIntensity intensity = rules.intensity();
        return strategy.apply(baseAmount, intensity);
    }

    // -------------------------
    // Invariants / guards
    // -------------------------

    private void ensureEditable() {
        if (status != TaxPolicyStatus.DRAFT) {
            throw new InvalidStateException(
                    "TAX_POLICY_NOT_EDITABLE",
                    "Only DRAFT policy is editable",
                    Map.of("currentStatus", status.name())
            );
        }
    }

    private void validateInvariants() {
        // TaxTarget valide déjà scope/value, mais on garde une protection
        if (target.scope() == null) {
            throw new DomainValidationException("TARGET_SCOPE_REQUIRED", "target scope is required", Map.of());
        }
        if (target.value() == null || target.value().isBlank()) {
            throw new DomainValidationException("TARGET_VALUE_REQUIRED", "target value is required", Map.of());
        }
        if (mode == null) {
            throw new DomainValidationException("TAX_MODE_REQUIRED", "tax mode is required", Map.of());
        }
        if (strategy == null) {
            throw new DomainValidationException("TAX_STRATEGY_REQUIRED", "tax strategy is required", Map.of());
        }
        if (rules == null) {
            throw new DomainValidationException("TAX_RULES_REQUIRED", "tax rules are required", Map.of());
        }
    }

    private static <T> T requireNonNull(T v, String name) {
        return Objects.requireNonNull(v, name + " must not be null");
    }

    // -------------------------
    // Getters
    // -------------------------

    public TaxPolicyId id() { return id; }
    public TransactionType transactionType() { return transactionType; }
    public TaxTarget target() { return target; }
    public TaxMode mode() { return mode; }
    public TaxStrategy strategy() { return strategy; }
    public TaxRules rules() { return rules; }
    public TaxPolicyStatus status() { return status; }

    public AuditInfo created() { return created; }
    public AuditInfo lastModified() { return lastModified; }
    public AuditInfo approvedOrRejected() { return approvedOrRejected; }
    public String blockReason() { return blockReason; }
}
