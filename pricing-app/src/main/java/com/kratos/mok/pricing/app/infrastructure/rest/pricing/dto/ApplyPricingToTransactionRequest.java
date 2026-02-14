package com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto;

import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApplyPricingToTransactionRequest(
        @NotBlank String externalTxId,
        @NotNull TransactionType transactionType,
        @NotBlank String amount,
        @NotBlank String currency,

        @NotBlank String accountId,
        @NotNull AccountType accountType,
        @NotNull Boolean kycValidated,
        @Min(0) int monthlyTxCount,
        @NotNull OffsetDateTime occurredAt,
        @NotNull Map<String, String> beneficiaryAccounts,

        // optionnel, pour pouvoir désactiver crédit externe en dev
        @NotNull Boolean creditExternalAccounts
) {}
