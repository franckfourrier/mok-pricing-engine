package com.kratos.mok.pricing.fees.domain;


import com.kratos.mok.pricing.fees.domain.enums.FeePolicyStatus;
import com.kratos.mok.pricing.fees.domain.enums.KycRequirement;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.strategy.FeeRules;
import com.kratos.mok.pricing.fees.domain.strategy.FeeStrategy;
import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.fees.domain.vo.PolicyPriority;
import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root: FeePolicy
 *
 * Concerne un type de transaction + une cible (GLOBAL / ACCOUNT_TYPE / ACCOUNT_ID).
 *
 * Règles de calcul appliquées:
 * - statut (ACTIVE, et non suspendu dans sa fenêtre)
 * - validité temporelle
 * - exigence KYC
 * - seuil d'activation (gratuit sous seuil)
 * - stratégie (FIXED / PERCENTAGE / TIERED)
 * - bornes min/max (plancher/plafond)
 * - condition volume (minMonthlyTxCount) optionnelle
 *
 * Workflow:
 * DRAFT -> PENDING_APPROVAL -> ACTIVE -> (SUSPENDED|ARCHIVED)
 * PENDING_APPROVAL -> REJECTED
 */
public class FeePolicy {

    private final FeePolicyId id;

    private final TransactionType transactionType;
    private final FeeTarget target;

    private FeeStrategy strategy;            // FIXED | PROPORTIONAL | TIERED
    private FeeRules rules;                  // threshold + min/max + volume condition
    private KycRequirement kycRequirement;

    private ValidityPeriod validity;         // start/end optionnel (null = permanent)
    private PolicyPriority priority;         // tie-breaker explicite

    private FeePolicyStatus status;          // DRAFT, PENDING_APPROVAL, ACTIVE, ...
    private SuspensionWindow suspension;     // optionnel

    private AuditInfo created;
    private AuditInfo lastModified;
    private AuditInfo approvedOrRejected;    // dernier acte de validation

    private FeePolicy(
            FeePolicyId id,
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeRules rules,
            KycRequirement kycRequirement,
            ValidityPeriod validity,
            PolicyPriority priority,
            FeePolicyStatus status,
            SuspensionWindow suspension,
            AuditInfo created,
            AuditInfo lastModified,
            AuditInfo approvedOrRejected
    ) {
        this.id = requireNonNull(id, "id");
        this.transactionType = requireNonNull(transactionType, "transactionType");
        this.target = requireNonNull(target, "target");
        this.strategy = requireNonNull(strategy, "strategy");
        this.rules = requireNonNull(rules, "rules");
        this.kycRequirement = (kycRequirement == null) ? KycRequirement.NONE : kycRequirement;
        this.validity = (validity == null) ? ValidityPeriod.permanent() : validity;
        this.priority = (priority == null) ? PolicyPriority.defaultFor(target.scope()) : priority;
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

    public static FeePolicy draft(
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeRules rules,
            KycRequirement kycRequirement,
            ValidityPeriod validity,
            PolicyPriority priority,
            String authorId,
            LocalDateTime when
    ) {
        var id = FeePolicyId.generate();
        var created = new AuditInfo(authorId, when, "DRAFT_CREATION");

        return new FeePolicy(
                id,
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

    /**
     * Reconstitution depuis la persistence.
     * (On vérifie les invariants, mais pas les règles de transition.)
     */
    public static FeePolicy reconstitute(
            FeePolicyId id,
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeRules rules,
            KycRequirement kycRequirement,
            ValidityPeriod validity,
            PolicyPriority priority,
            FeePolicyStatus status,
            SuspensionWindow suspension,
            AuditInfo created,
            AuditInfo lastModified,
            AuditInfo approvedOrRejected
    ) {
        return new FeePolicy(
                id,
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
    }

    // -------------------------
    // Commands (business methods)
    // -------------------------

    public void updateConfiguration(
            FeeStrategy newStrategy,
            FeeRules newRules,
            KycRequirement newKycRequirement,
            ValidityPeriod newValidity,
            PolicyPriority newPriority,
            String authorId,
            LocalDateTime when,
            String reason
    ) {
        ensureEditable();

        this.strategy = requireNonNull(newStrategy, "newStrategy");
        this.rules = requireNonNull(newRules, "newRules");
        this.kycRequirement = (newKycRequirement == null) ? KycRequirement.NONE : newKycRequirement;
        this.validity = (newValidity == null) ? ValidityPeriod.permanent() : newValidity;
        this.priority = (newPriority == null) ? PolicyPriority.defaultFor(target.scope()) : newPriority;
        this.lastModified = new AuditInfo(authorId, when, reason == null ? "UPDATE" : reason);

        validateInvariants();
    }

    public void submitForApproval(String authorId, LocalDateTime when, String reason) {
        if (status != FeePolicyStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT policies can be submitted for approval.");
        }
        this.status = FeePolicyStatus.PENDING_APPROVAL;
        this.lastModified = new AuditInfo(authorId, when, reason == null ? "SUBMIT_FOR_APPROVAL" : reason);
    }

    public void approve(String superAdminId, LocalDateTime when, String justification) {
        if (status != FeePolicyStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL policies can be approved.");
        }
        this.status = FeePolicyStatus.ACTIVE;
        this.suspension = null;
        this.approvedOrRejected = new AuditInfo(superAdminId, when, justification == null ? "APPROVED" : justification);
        this.lastModified = this.approvedOrRejected;
    }

    public void reject(String superAdminId, LocalDateTime when, String justification) {
        if (status != FeePolicyStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL policies can be rejected.");
        }
        this.status = FeePolicyStatus.REJECTED;
        this.approvedOrRejected = new AuditInfo(superAdminId, when, justification == null ? "REJECTED" : justification);
        this.lastModified = this.approvedOrRejected;
    }

    /**
     * Suspension temporaire (campagne promo).
     * Une policy suspendue ne s'applique pas tant que la fenêtre est active.
     */
    public void suspend(LocalDateTime from, LocalDateTime to, String actorId, LocalDateTime when, String reason) {
        if (status != FeePolicyStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE policies can be suspended.");
        }
        this.status = FeePolicyStatus.SUSPENDED;
        this.suspension = new SuspensionWindow(from, to);
        this.lastModified = new AuditInfo(actorId, when, reason == null ? "SUSPEND" : reason);
    }

    /**
     * Reprise manuelle.
     * (Optionnel: reprise auto côté Query/Resolver si now > suspension.to)
     */
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

    // -------------------------
    // Query (pure domain)
    // -------------------------

    /**
     * Calcule le frais selon les règles.
     * Renvoie un résultat détaillé utile pour audit/notification.
     */
    public FeeComputation computeFee(Money transactionAmount, TransactionContext ctx, LocalDateTime now) {
        requireNonNull(transactionAmount, "transactionAmount");
        requireNonNull(ctx, "ctx");
        var at = (now == null) ? LocalDateTime.now() : now;

        ensureApplicable(at, ctx);

        // seuil gratuit
        if (rules.activationThreshold() != null && transactionAmount.compareTo(rules.activationThreshold()) < 0) {
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

        String ruleApplied = strategyRuleApplied(strategy.type());

        return new FeeComputation(
                id,
                transactionAmount,
                bounded,
                strategy.type(),
                ruleApplied,
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

    // -------------------------
    // Internal checks
    // -------------------------

    private void ensureApplicable(LocalDateTime at, TransactionContext ctx) {
        if (status != FeePolicyStatus.ACTIVE && status != FeePolicyStatus.SUSPENDED) {
            throw new IllegalStateException("FeePolicy not applicable (status=" + status + ")");
        }

        if (!validity.isValidAt(at)) {
            throw new IllegalStateException("FeePolicy not valid at " + at);
        }

        // Suspension active => non applicable
        if (status == FeePolicyStatus.SUSPENDED) {
            if (suspension == null) {
                throw new IllegalStateException("Policy is SUSPENDED but suspension window is missing");
            }
            if (suspension.isActiveAt(at)) {
                throw new IllegalStateException("FeePolicy suspended until " + suspension.to());
            }
            // suspension expirée: on peut considérer applicable,
            // la transition persistée peut être faite par un handler/job.
        }

        if (kycRequirement == KycRequirement.REQUIRED && !ctx.isKycValidated()) {
            throw new IllegalStateException("KYC is required but not verified");
        }

        // Condition volume
        if (rules.minMonthlyTxCount().isPresent()
                && ctx.monthlyTransactionCount() < rules.minMonthlyTxCount().get()) {
            throw new IllegalStateException("Monthly tx count requirement not met");
        }
    }

    private void ensureEditable() {
        if (status != FeePolicyStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT policy is editable.");
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

    // -------------------------
    // Getters
    // -------------------------

    public FeePolicyId id() { return id; }

    public TransactionType transactionType() { return transactionType; }

    public FeeTarget target() { return target; }

    public FeeStrategy strategy() { return strategy; }

    public FeeRules rules() { return rules; }

    public KycRequirement kycRequirement() { return kycRequirement; }

    public ValidityPeriod validity() { return validity; }

    public PolicyPriority priority() { return priority; }

    public FeePolicyStatus status() { return status; }

    public Optional<SuspensionWindow> suspension() { return Optional.ofNullable(suspension); }

    public AuditInfo created() { return created; }

    public Optional<AuditInfo> lastModified() { return Optional.ofNullable(lastModified); }

    public Optional<AuditInfo> approvedOrRejected() { return Optional.ofNullable(approvedOrRejected); }
}
