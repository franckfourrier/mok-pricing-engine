package com.kratos.mok.pricing.app.infrastructure.repository.cantonment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "cantonment_credits",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cantonment_reference",
                columnNames = "payment_reference"
        )
)
@Getter
@Setter
public class CantonmentCreditEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "payment_reference", nullable = false, updatable = false, length = 120)
    private String paymentReference;

    @Column(name = "amount", nullable = false, length = 40)
    private String amount;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @Column(name = "super_distributor_id", nullable = false, length = 120)
    private String superDistributorId;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // RECEIVED/APPLIED/DUPLICATE/REJECTED

    @Column(name = "ledger_external_tx_id", length = 150)
    private String ledgerExternalTxId;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
}
