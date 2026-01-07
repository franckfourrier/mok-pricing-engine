package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.fees.domain.enums.PolicyStatus;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.snapshot.FeePolicySnapshot;
import com.kratos.mok.pricing.fees.domain.strategy.FeeStrategy;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;
import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.LocalDateTime;

public class FeePolicy {

    private final FeePolicyId id;
    private final TransactionType transactionType;
    private final FeeTarget feeTarget;
    private FeeStrategy strategy;
    private FeeLimits limits;
    private Money activationThreshold;
    private ValidityPeriod validity;
    private boolean kycRequired;
    private PolicyStatus status;
    private AuditInfo createdBy;
    private AuditInfo lastModifiedBy;

    private FeePolicy(FeePolicyId id, TransactionType type, FeeTarget target, FeeStrategy strategy,
                      FeeLimits limits, Money activationThreshold, ValidityPeriod validity,
                      boolean kycRequired, AuditInfo createdBy) {
        this.id = id;
        this.transactionType = type;
        this.feeTarget = target;
        this.strategy = strategy;
        this.limits = limits != null ? limits : FeeLimits.NONE;
        this.activationThreshold = activationThreshold != null ? activationThreshold : Money.ZERO;
        this.validity = validity != null ? validity : ValidityPeriod.PERMANENT;
        this.kycRequired = kycRequired;
        this.createdBy = createdBy;
        this.status = PolicyStatus.PENDING_VALIDATION;
    }

    public static FeePolicy create(TransactionType type, FeeTarget target, FeeStrategy strategy,
                                   FeeLimits limits, Money activationThreshold, ValidityPeriod validity,
                                   boolean kycRequired, String authorId) {
        return new FeePolicy(
                FeePolicyId.generate(), type, target, strategy,
                limits, activationThreshold, validity, kycRequired,
                new AuditInfo(authorId, LocalDateTime.now(), "Initial creation")
        );
    }

    public void activate(String superAdminId) {
        if (this.status == PolicyStatus.ACTIVE) return;
        this.status = PolicyStatus.ACTIVE;
        this.lastModifiedBy = new AuditInfo(superAdminId, LocalDateTime.now(), "Super-Admin Validation");
    }

    public Money calculateFee(TransactionContext context) {
        // 1. Contrôle Cycle de vie
        if (this.status != PolicyStatus.ACTIVE) {
            throw new IllegalStateException("Rule not active (Status: " + status + ")");
        }

        // 2. Contrôle Temporel
        if (!validity.isValid(context.transactionDate())) {
            throw new IllegalArgumentException("Rule expired");
        }

        // 3. Contrôle Conditionnel (KYC)
        if (kycRequired && !context.isKycValidated()) {
            throw new IllegalArgumentException("KYC required");
        }

        // 4. Contrôle Seuil d'activation ("Gratuit jusqu'à 5000")
        if (context.amount().compareTo(activationThreshold) <= 0) {
            return Money.ZERO;
        }

        // 5. Calcul théorique (Stratégie)
        Money baseFee = strategy.apply(context.amount());

        // 6. Application des Bornes (Min/Max)
        return limits.apply(baseFee);
    }

    public void suspend(String authorId, String reason) {
        this.status = PolicyStatus.SUSPENDED;
        this.lastModifiedBy = new AuditInfo(authorId, LocalDateTime.now(), "Suspension: " + reason);
    }

    public int priority() {
        return feeTarget.scope().priority();
    }

    public FeePolicySnapshot snapshot() {
        return new FeePolicySnapshot(
                id.toString(),
                transactionType.name(),
                feeTarget.scope().name(),
                feeTarget.value(),
                strategy.getClass().getSimpleName(), // Ex: "FixedFee"
                null,
                (limits.minAmount() != null) ? limits.minAmount().amount().toString() : null,
                (limits.maxAmount() != null) ? limits.maxAmount().amount().toString() : null,
                activationThreshold.amount().toString(),
                kycRequired,
                status.name(),
                createdBy.author(),
                createdBy.timestamp(),
                (lastModifiedBy != null) ? lastModifiedBy.author() : null,
                (lastModifiedBy != null) ? lastModifiedBy.timestamp() : null
        );
    }

    public static FeePolicy reconstitute(
            FeePolicyId id,
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeLimits limits,
            Money activationThreshold,
            ValidityPeriod validity,
            boolean kycRequired,
            PolicyStatus status,
            AuditInfo createdBy,
            AuditInfo lastModifiedBy
    ) {
        FeePolicy policy = new FeePolicy(
                id,
                transactionType,
                target,
                strategy,
                limits,
                activationThreshold,
                validity,
                kycRequired,
                createdBy
        );

        policy.status = status;
        policy.lastModifiedBy = lastModifiedBy;

        return policy;
    }

    public FeeStrategy strategy() {
        return this.strategy;
    }

    public FeePolicyId id() {
        return id;
    }
}
