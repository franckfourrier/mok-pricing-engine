package com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto.updateCommissionPlan;

import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateCommissionPlanRequest(
        @NotNull TransactionCode transactionCode,
        @Valid List<KeyRequest> keys
) {
    public record KeyRequest(
            @NotBlank String beneficiary,
            @NotBlank String percentage
    ) {}
}