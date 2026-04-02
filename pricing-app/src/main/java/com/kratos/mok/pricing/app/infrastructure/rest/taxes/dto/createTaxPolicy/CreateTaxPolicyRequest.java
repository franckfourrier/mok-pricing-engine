package com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto.createTaxPolicy;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateTaxPolicyRequest(
        @NotEmpty List<@NotNull TransactionCode> transactionCodes,
        //@NotNull TargetScope targetScope,
        //@NotBlank String targetValue,

        @NotBlank String currency,               // "XAF"

        TaxMode mode,         // CANTONNEMENT / EXPLOITATION
        @NotNull TaxStrategyType strategyType,   // ELECTRONIC_RATE / FIXED_AMOUNT

        // ELECTRONIC_RATE
        String rate,                             // ex "0.015"

        // FIXED_AMOUNT
        String fixedAmount,                      // ex "50"

        // optional
        String fluxIntensity,                    // ex "1"
        Boolean exempted                         // optionnel (sinon false)
) {}
