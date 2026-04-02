package com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record CreateCommissionPlanRequest(

        @NotNull TransactionCode transactionCode,
        //@NotNull TargetScope targetScope,
        //@NotBlank String targetValue,
        @Valid List<KeyRequest> keys,
        String agentPercentage,
        String coverageRate,
        LocalDateTime validityStart,
        LocalDateTime validityEnd
) {
    public record KeyRequest(
            @NotBlank String beneficiary,
            @NotBlank String percentage
    ) {}
}
