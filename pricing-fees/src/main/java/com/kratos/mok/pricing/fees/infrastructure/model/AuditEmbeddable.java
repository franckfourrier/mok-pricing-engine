package com.kratos.mok.pricing.fees.infrastructure.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditEmbeddable {

    private String author;

    private LocalDateTime timestamp;

    private String reason;
}
