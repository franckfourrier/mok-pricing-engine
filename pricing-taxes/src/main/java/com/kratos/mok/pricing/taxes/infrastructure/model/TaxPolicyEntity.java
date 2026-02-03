package com.kratos.mok.pricing.taxes.infrastructure.model;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxPolicyStatus;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_policies",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tax_policy_perimeter",
                columnNames = {"transaction_type", "target_scope", "target_value"}
        ))
@Getter
@Setter
public class TaxPolicyEntity {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 40)
    private TransactionType transactionType;

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

    // ELECTRONIC_RATE
    @Column(name = "rate", precision = 19, scale = 8)
    private BigDecimal rate;

    // FIXED_AMOUNT
    @Column(name = "fixed_amount", precision = 19, scale = 2)
    private BigDecimal fixedAmount;

    // Rules
    @Column(name = "flux_intensity", precision = 19, scale = 8, nullable = false)
    private BigDecimal fluxIntensity;

    @Column(name = "exempted", nullable = false)
    private boolean exempted;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TaxPolicyStatus status;

    @Column(name = "block_reason", length = 120)
    private String blockReason;

    @Column(name = "created_by", nullable = false, length = 80)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_modified_by", length = 80)
    private String lastModifiedBy;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Column(name = "approved_by", length = 80)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
