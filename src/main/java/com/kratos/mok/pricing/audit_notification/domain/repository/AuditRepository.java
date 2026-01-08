package com.kratos.mok.pricing.audit_notification.domain.repository;

import com.kratos.mok.pricing.audit_notification.domain.AuditLog;

public interface AuditRepository {
    void save(AuditLog log);
}
