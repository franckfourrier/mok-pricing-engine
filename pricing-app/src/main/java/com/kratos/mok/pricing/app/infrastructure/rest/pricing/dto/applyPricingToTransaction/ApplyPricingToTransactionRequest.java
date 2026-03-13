package com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.applyPricingToTransaction;

import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

public record ApplyPricingToTransactionRequest(
        @NotBlank String externalTxId,
        @NotNull TransactionCode transactionCode,
        @NotBlank String amount,
        String currency,

        @NotBlank String accountId,
        @NotNull AccountType accountType,
        @NotNull Boolean kycValidated,
        @Min(0) int monthlyTxCount,
        @NotNull OffsetDateTime occurredAt,

        String distributorAccountId,
        String superDistributorAccountId
) {}
