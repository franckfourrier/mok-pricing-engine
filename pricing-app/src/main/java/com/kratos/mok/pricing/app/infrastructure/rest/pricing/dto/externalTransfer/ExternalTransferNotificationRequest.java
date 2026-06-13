package com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.externalTransfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;

public record ExternalTransferNotificationRequest(
    @NotBlank String referencePayment,
    @NotBlank String amount,
    @NotBlank String currency,
    @NotBlank String partnerId,
    @NotNull(message = "occurredAt is required")
    @PastOrPresent(message = "occurredAt must be in the past or present")
    OffsetDateTime occurredAt
) {
}
