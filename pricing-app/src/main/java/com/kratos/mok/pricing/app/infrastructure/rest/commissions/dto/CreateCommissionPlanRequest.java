package com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto;

import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateCommissionPlanRequest(

        @NotNull TransactionCode transactionCode,
        //@NotNull TargetScope targetScope,
        //@NotBlank String targetValue,
        @Valid List<KeyRequest> keys,
        OffsetDateTime validityStart,
        OffsetDateTime validityEnd
) {
    public record KeyRequest(
            @NotBlank String beneficiary,
            @NotBlank String percentage
    ) {}
}
