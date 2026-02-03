package com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto;

import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateTaxPolicyRequest(

        @NotNull TransactionType type,

        @NotNull TargetScope targetScope,

        @NotBlank String targetValue,

        @NotBlank String currency, // "XAF"

        @NotNull TaxMode mode,

        @NotNull TaxStrategyType strategyType,

        // ELECTRONIC_RATE
        String rate,

        // FIXED_AMOUNT
        String fixedAmount,

        // optional
        String fluxIntensity,

        @NotBlank String activationThreshold,
        String minTax,
        String maxTax,

        LocalDateTime validityStart,
        LocalDateTime validityEnd
) {}
