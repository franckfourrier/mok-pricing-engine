package com.kratos.mok.pricing.auditNotification.infrastructure.repository;

import com.kratos.mok.pricing.auditNotification.domain.AuditLog;
import com.kratos.mok.pricing.auditNotification.domain.repository.AuditRepository;
import com.kratos.mok.pricing.auditNotification.infrastructure.entity.AuditLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostgresAuditRepository implements AuditRepository {

    private final JpaAuditRepository jpaRepository;

    @Override
    public void save(AuditLog log) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(log.id());
        entity.setModule(log.module());
        entity.setAction(log.action());
        entity.setAggregateId(log.aggregateId());
        entity.setActor(log.actor());
        entity.setReason(log.reason());
        entity.setPayload(log.payloadJson());
        entity.setTimestamp(log.timestamp());

        jpaRepository.save(entity);
    }
}

