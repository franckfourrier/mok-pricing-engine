package com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto.updateCommissionPlan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateCommissionPlanRequest(
        @Valid List<KeyRequest> keys
) {
    public record KeyRequest(
            @NotBlank String beneficiary,
            @NotBlank String percentage
    ) {}
}