package com.kratos.mok.pricing.audit_notification.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter
public class AuditLogEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String module;

    @Column(nullable = false)
    private String action;

    @Column(name = "aggregate_id")
    private String aggregateId;

    @Column(nullable = false)
    private String actor;

    private String reason;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;
}
