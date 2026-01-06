package com.kratos.mok.pricing.shared.domain.vo;

import java.time.LocalDateTime;

public record AuditInfo(String author, LocalDateTime timestamp, String reason) {}

