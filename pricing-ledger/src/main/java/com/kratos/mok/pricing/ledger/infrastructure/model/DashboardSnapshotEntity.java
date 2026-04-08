package com.kratos.mok.pricing.ledger.infrastructure.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ledger_dashboard_snapshot")
@Getter
@Setter
public class DashboardSnapshotEntity {

    @Id
    @Column(name = "account_code", length = 50)
    private String accountCode;

    @Column(name = "balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(name = "currency", length = 8, nullable = false)
    private String currency;

    @Column(name = "last_variation", precision = 19, scale = 2)
    private BigDecimal lastVariation;

    @Column(name = "last_trend", length = 10)
    private String lastTrend;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}