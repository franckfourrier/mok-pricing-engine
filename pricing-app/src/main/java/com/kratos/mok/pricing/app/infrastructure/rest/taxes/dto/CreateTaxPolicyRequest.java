package com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaxPolicyRequest(
        @NotNull TransactionType type,
        @NotNull TargetScope targetScope,
        @NotBlank String targetValue,

        @NotBlank String currency,               // "XAF"

        @NotNull TaxMode mode,                   // CANTONNEMENT / EXPLOITATION
        @NotNull TaxStrategyType strategyType,   // ELECTRONIC_RATE / FIXED_AMOUNT

        // ELECTRONIC_RATE
        String rate,                             // ex "0.015"

        // FIXED_AMOUNT
        String fixedAmount,                      // ex "50"

        // optional
        String fluxIntensity,                    // ex "1"
        Boolean exempted                         // optionnel (sinon false)
) {}
