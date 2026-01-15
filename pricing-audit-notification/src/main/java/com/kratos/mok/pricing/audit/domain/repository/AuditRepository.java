package com.kratos.mok.pricing.audit.domain.repository;

import com.kratos.mok.pricing.audit.domain.AuditLog;

public interface AuditRepository {
    void save(AuditLog log);
}
