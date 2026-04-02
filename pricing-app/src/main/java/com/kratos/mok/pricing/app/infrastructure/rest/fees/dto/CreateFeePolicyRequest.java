package com.kratos.mok.pricing.app.infrastructure.rest.fees.dto;

import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.fees.domain.enums.KycRequirement;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record CreateFeePolicyRequest(

        @NotNull TransactionCode transactionCode,

        //@NotNull TargetScope targetScope,

        //@NotBlank String targetValue,

        @NotBlank String currency,

        @NotNull FeeStrategyType strategyType,

        String fixedAmount,

        String percentage,

        @Valid List<TierRequest> tiers,

        @NotBlank String activationThreshold,

        String minFee,
        String maxFee,

        LocalDateTime validityStart,
        LocalDateTime validityEnd,

        @NotNull KycRequirement kycRequirement,

        Integer minMonthlyTxCount
) {
    public record TierRequest(
            @NotBlank String min,
            @NotBlank String max,
            @NotNull FeeStrategyType tierStrategyType,
            @NotBlank String tierValue
    ) {}
}
