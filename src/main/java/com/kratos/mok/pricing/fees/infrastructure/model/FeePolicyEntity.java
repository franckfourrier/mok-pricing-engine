package com.kratos.mok.pricing.fees.infrastructure.model;

import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.strategy.FeeStrategy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fee_policies")
@Getter @Setter
public class FeePolicyEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "target_scope", nullable = false)
    private String targetScope;

    @Column(name = "target_value", nullable = false)
    private String targetValue;

    @Column(name = "priority", nullable = false)
    private int priority;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strategy_json", columnDefinition = "jsonb", nullable = false)
    private FeeStrategy strategy;

    @Column(name = "limit_min")
    private BigDecimal limitMin;

    @Column(name = "limit_max")
    private BigDecimal limitMax;

    @Column(name = "activation_threshold")
    private BigDecimal activationThreshold;

    @Column(name = "validity_start")
    private LocalDateTime validityStart;

    @Column(name = "validity_end")
    private LocalDateTime validityEnd;

    @Column(name = "kyc_required")
    private boolean kycRequired;

    @Column(name = "status", nullable = false)
    private String status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "author", column = @Column(name = "created_by")),
            @AttributeOverride(name = "timestamp", column = @Column(name = "created_at")),
            @AttributeOverride(name = "reason", column = @Column(name = "created_reason"))
    })
    private AuditEmbeddable createdBy;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "author", column = @Column(name = "modified_by")),
            @AttributeOverride(name = "timestamp", column = @Column(name = "modified_at")),
            @AttributeOverride(name = "reason", column = @Column(name = "modified_reason"))
    })
    private AuditEmbeddable lastModifiedBy;
}
