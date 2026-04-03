package com.kratos.mok.pricing.taxes.infrastructure.model;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.infrastructure.config.model.AuditEmbeddable;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxPolicyStatus;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "tax_policies",
        indexes = {
                @Index(name = "idx_tax_scope_value", columnList = "target_scope, target_value")
        }
)
@Getter
@Setter
public class TaxPolicyEntity {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "tax_policy_transaction_codes",
            joinColumns = @JoinColumn(name = "tax_policy_id")
    )
    @Column(name = "transaction_code", nullable = false, length = 80)
    @Enumerated(EnumType.STRING)
    private Set<TransactionCode> transactionCodes = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "target_scope", nullable = false, length = 30)
    private TargetScope targetScope;

    @Column(name = "target_value", nullable = false, length = 80)
    private String targetValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_mode", nullable = false, length = 30)
    private TaxMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy_type", nullable = false, length = 30)
    private TaxStrategyType strategyType;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @Column(name = "rate", precision = 19, scale = 8)
    private BigDecimal rate;

    @Column(name = "fixed_amount", precision = 19, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "flux_intensity", precision = 19, scale = 8, nullable = false)
    private BigDecimal fluxIntensity;

    @Column(name = "exempted", nullable = false)
    private boolean exempted;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TaxPolicyStatus status;

    @Column(name = "block_reason", length = 120)
    private String blockReason;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "author", column = @Column(name = "created_by", nullable = false, length = 80)),
            @AttributeOverride(name = "timestamp", column = @Column(name = "created_at", nullable = false)),
            @AttributeOverride(name = "reason", column = @Column(name = "created_reason", nullable = false))
    })
    private AuditEmbeddable createdBy;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "author", column = @Column(name = "last_modified_by", length = 80)),
            @AttributeOverride(name = "timestamp", column = @Column(name = "last_modified_at")),
            @AttributeOverride(name = "reason", column = @Column(name = "last_modified_reason"))
    })
    private AuditEmbeddable lastModifiedBy;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "author", column = @Column(name = "approved_by", length = 80)),
            @AttributeOverride(name = "timestamp", column = @Column(name = "approved_at")),
            @AttributeOverride(name = "reason", column = @Column(name = "approved_reason"))
    })
    private AuditEmbeddable approvedOrRejectedBy;
}
