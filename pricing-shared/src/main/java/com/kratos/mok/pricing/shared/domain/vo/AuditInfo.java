package com.kratos.mok.pricing.shared.domain.vo;

import java.time.OffsetDateTime;

public record AuditInfo(String author, OffsetDateTime timestamp, String reason) {}

