package com.kratos.mok.pricing.shared.infrastructure.config.model;

import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class AuditEmbeddable {

    private String author;
    private LocalDateTime timestamp;
    private String reason;

    protected AuditEmbeddable() {}

    public AuditEmbeddable(String author, LocalDateTime timestamp, String reason) {
        this.author = author;
        this.timestamp = timestamp;
        this.reason = reason;
    }

    public static AuditEmbeddable fromDomain(AuditInfo info) {
        if (info == null) return null;
        return new AuditEmbeddable(info.author(), info.timestamp(), info.reason());
    }

    public AuditInfo toDomain() {
        if (author == null && timestamp == null && reason == null) return null;
        return new AuditInfo(author, timestamp, reason);
    }

    public String getAuthor() { return author; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getReason() { return reason; }

    public void setAuthor(String author) { this.author = author; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setReason(String reason) { this.reason = reason; }
}
