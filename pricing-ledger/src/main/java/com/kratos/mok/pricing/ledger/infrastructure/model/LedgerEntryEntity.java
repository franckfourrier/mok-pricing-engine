package com.kratos.mok.pricing.ledger.infrastructure.model;

import com.kratos.mok.pricing.ledger.domain.enums.EntryDirection;
import com.kratos.mok.pricing.ledger.domain.enums.LedgerEntryKind;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "ledger_entries",
        indexes = {
                @Index(name = "idx_ledger_acc_curr_time", columnList = "account_code, currency, occurred_at"),
                @Index(name = "idx_ledger_external_tx", columnList = "external_tx_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ledger_external_tx_line", columnNames = {"external_tx_id", "line_no"})
        }
)
public class LedgerEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "external_tx_id", nullable = false, length = 120)
    private String externalTxId;

    @Column(name = "line_no", nullable = false)
    private int lineNo;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "account_code", nullable = false, length = 50)
    private String accountCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private EntryDirection direction;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 50)
    private LedgerEntryKind kind;

    @Column(name = "policy_id", length = 120)
    private String policyId;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "created_by", nullable = false, length = 80)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
