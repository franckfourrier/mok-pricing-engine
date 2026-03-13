package com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto.updateTaxPolicy;

import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateTaxPolicyRequest(
        @NotEmpty List<@NotNull TransactionCode> transactionCodes,
        TaxMode mode,
        @NotNull TaxStrategyType strategyType,
        String rate,
        String fixedAmount,
        String fluxIntensity,
        Boolean exempted
) {}