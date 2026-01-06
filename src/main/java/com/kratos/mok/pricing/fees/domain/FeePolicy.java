package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.fees.domain.enums.AccountType;
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
    private final AccountType targetAccount;
    private FeeStrategy strategy;
    private PolicyStatus status;
    private AuditInfo createdBy;
    private AuditInfo lastModifiedBy;

    private FeePolicy(FeePolicyId id, TransactionType type, AccountType target, FeeStrategy strategy, AuditInfo createdBy) {
        this.id = id;
        this.transactionType = type;
        this.targetAccount = target;
        this.strategy = strategy;
        this.createdBy = createdBy;
        this.status = PolicyStatus.PENDING_VALIDATION;
    }

    public static FeePolicy create(TransactionType type, AccountType target, FeeStrategy strategy, String authorId) {
        return new FeePolicy(
                FeePolicyId.generate(),
                type,
                target,
                strategy,
                new AuditInfo(authorId, LocalDateTime.now(), "Initial creation")
        );
    }

    public void activate(String superAdminId) {
        if (this.status == PolicyStatus.ACTIVE) return;
        this.status = PolicyStatus.ACTIVE;
        this.lastModifiedBy = new AuditInfo(superAdminId, LocalDateTime.now(), "Super-Admin Validation");
    }

    public Money calculateFee(Money amount) {
        if (this.status != PolicyStatus.ACTIVE) {
            throw new IllegalStateException("Unable to apply an inactive rule (Status: " + status + ")");
        }
        return strategy.apply(amount);
    }

    public void suspend(String authorId, String reason) {
        this.status = PolicyStatus.SUSPENDED;
        this.lastModifiedBy = new AuditInfo(authorId, LocalDateTime.now(), "Suspension: " + reason);
    }

    public FeePolicySnapshot snapshot() {
        return new FeePolicySnapshot(
                id.toString(),
                transactionType.name(),
                targetAccount.name(),
                strategy.toString(),
                status.name(),
                createdBy.toString(),
                (this.lastModifiedBy != null) ? this.lastModifiedBy.toString() : null

        );
    }
}
