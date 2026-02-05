package com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto;

import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ApplyPricingToTransactionRequest(
        @NotBlank String externalTxId,
        @NotNull TransactionType transactionType,
        @NotBlank String amount,
        String currency,
        @NotBlank String accountId,
        @NotNull AccountType accountType,
        @NotNull Boolean kycValidated,
        @Min(0) int monthlyTxCount,
        @NotNull OffsetDateTime occurredAt
) {}
