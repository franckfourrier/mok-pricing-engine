package com.kratos.mok.pricing.fees.infrastructure.model;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.strategy.FeeStrategy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_policies")
@Getter
@Setter
public class FeePolicyEntity {

    // ------------------------------------------------------------------
    // Identity
    // ------------------------------------------------------------------

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    // ------------------------------------------------------------------
    // Scope
    // ------------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 40)
    private TransactionType transactionType;

    /*@Column(name = "target_scope", nullable = false, length = 30)
    private String targetScope; // TargetScope.name()*/

    @Enumerated(EnumType.STRING)
    @Column(name = "target_scope", nullable = false, length = 30)
    private TargetScope targetScope;

    @Column(name = "target_value", nullable = false, length = 80)
    private String targetValue;

    @Column(name = "priority", nullable = false)
    private int priority;

    // ------------------------------------------------------------------
    // Strategy
    // ------------------------------------------------------------------

    /**
     * FIXED / PROPORTIONAL / TIERED
     * Persistée en JSONB (structure complète).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strategy_json", columnDefinition = "jsonb", nullable = false)
    private FeeStrategy strategy;

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    @Column(name = "activation_threshold", precision = 19, scale = 2)
    private BigDecimal activationThreshold;

    @Column(name = "min_fee", precision = 19, scale = 2)
    private BigDecimal minFee;

    @Column(name = "max_fee", precision = 19, scale = 2)
    private BigDecimal maxFee;

    @Column(name = "min_monthly_tx_count")
    private Integer minMonthlyTxCount;

    // ------------------------------------------------------------------
    // Validity
    // ------------------------------------------------------------------

    @Column(name = "validity_start")
    private LocalDateTime validityStart;

    @Column(name = "validity_end")
    private LocalDateTime validityEnd;

    // ------------------------------------------------------------------
    // KYC (enum côté domaine, boolean en base)
    // ------------------------------------------------------------------

    /**
     * true  -> KycRequirement.REQUIRED
     * false -> KycRequirement.NONE
     */
    @Column(name = "kyc_required", nullable = false)
    private boolean kycRequired;

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    @Column(name = "status", nullable = false, length = 30)
    private String status; // FeePolicyStatus.name()

    /**
     * Code métier de blocage (ex: CONFLICTING_POLICY, BEAC_NON_COMPLIANT)
     */
    @Column(name = "block_reason", length = 120)
    private String blockReason;

    // ------------------------------------------------------------------
    // Suspension
    // ------------------------------------------------------------------

    @Column(name = "suspension_from")
    private LocalDateTime suspensionFrom;

    @Column(name = "suspension_to")
    private LocalDateTime suspensionTo;

    // ------------------------------------------------------------------
    // Audit — Created
    // ------------------------------------------------------------------

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "author", column = @Column(name = "created_by", nullable = false)),
            @AttributeOverride(name = "timestamp", column = @Column(name = "created_at", nullable = false)),
            @AttributeOverride(name = "reason", column = @Column(name = "created_reason", nullable = false))
    })
    private AuditEmbeddable createdBy;

    // ------------------------------------------------------------------
    // Audit — Last modification
    // ------------------------------------------------------------------

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "author", column = @Column(name = "modified_by")),
            @AttributeOverride(name = "timestamp", column = @Column(name = "modified_at")),
            @AttributeOverride(name = "reason", column = @Column(name = "modified_reason"))
    })
    private AuditEmbeddable lastModifiedBy;

    // ------------------------------------------------------------------
    // Audit — Approval / Rejection
    // ------------------------------------------------------------------

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "author", column = @Column(name = "approved_by")),
            @AttributeOverride(name = "timestamp", column = @Column(name = "approved_at")),
            @AttributeOverride(name = "reason", column = @Column(name = "approved_reason"))
    })
    private AuditEmbeddable approvedOrRejectedBy;
}
