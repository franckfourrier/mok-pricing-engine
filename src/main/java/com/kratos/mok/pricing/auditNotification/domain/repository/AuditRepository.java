package com.kratos.mok.pricing.auditNotification.domain.repository;

import com.kratos.mok.pricing.auditNotification.domain.AuditLog;

public interface AuditRepository {
    void save(AuditLog log);
}
