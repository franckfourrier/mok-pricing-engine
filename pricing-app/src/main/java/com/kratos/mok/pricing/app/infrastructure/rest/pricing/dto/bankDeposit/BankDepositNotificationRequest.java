package com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.bankDeposit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record BankDepositNotificationRequest(
        @NotBlank String referencePayment,
        @NotBlank String amount,
        @NotBlank String currency,
        @NotBlank String superDistributorId,
        @NotNull OffsetDateTime occurredAt
) {}
