package com.kratos.mok.pricing.commissions.infrastructure.model;

import com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus;
import com.kratos.mok.pricing.commissions.domain.strategy.CommissionStrategy;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.infrastructure.config.model.AuditEmbeddable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "commission_plans",
        indexes = {
                @Index(name = "idx_commission_plans_tt_status", columnList = "transaction_type, status"),
                @Index(name = "idx_commission_plans_scope_value", columnList = "target_scope, target_value"),
                @Index(name = "idx_commission_plans_validity", columnList = "validity_start, validity_end"),
                @Index(name = "idx_commission_plans_priority", columnList = "priority")
        }
)
@Getter
@Setter
public class CommissionPlanEntity {

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
    @Column(name = "transaction_code", nullable = false, length = 80)
    private TransactionCode transactionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 40)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_scope", nullable = false, length = 30)
    private TargetScope targetScope;

    @Column(name = "target_value", nullable = false, length = 120)
    private String targetValue;

    @Column(name = "priority", nullable = false)
    private int priority;

    // ------------------------------------------------------------------
    // Strategy (JSONB)
    // ------------------------------------------------------------------

    /**
     * DIRECT / DEPOSIT_DISTRIBUTION / WITHDRAWAL_AGENT_KRATOS
     * Persistée en JSONB, sérialisation Jackson via @JsonTypeInfo.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strategy_json", columnDefinition = "jsonb", nullable = false)
    private CommissionStrategy strategy;

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CommissionPlanStatus status;

    @Column(name = "block_reason", length = 120)
    private String blockReason;

    // ------------------------------------------------------------------
    // Validity
    // ------------------------------------------------------------------

    @Column(name = "validity_start")
    private LocalDateTime validityStart;

    @Column(name = "validity_end")
    private LocalDateTime validityEnd;

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
            @AttributeOverride(name = "author", column = @Column(name = "last_modified_by")),
            @AttributeOverride(name = "timestamp", column = @Column(name = "last_modified_at")),
            @AttributeOverride(name = "reason", column = @Column(name = "last_modified_reason"))
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

    /*@Version TODO
    private long version;*/
}
