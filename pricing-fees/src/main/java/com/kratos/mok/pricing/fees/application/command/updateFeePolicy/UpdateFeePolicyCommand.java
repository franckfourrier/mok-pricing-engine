package com.kratos.mok.pricing.fees.application.command.updateFeePolicy;

import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.fees.domain.enums.KycRequirement;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateFeePolicyCommand(
        String policyId,
        TransactionCode transactionCode,
        TargetScope targetScope,
        String targetValue,
        String currency,
        FeeStrategyType strategyType,
        String fixedAmount,
        String percentage,
        List<TierCommand> tiers,
        String activationThreshold,
        String minFee,
        String maxFee,
        LocalDateTime validityStart,
        LocalDateTime validityEnd,
        KycRequirement kycRequirement,
        Integer minMonthlyTxCount
) {
    public record TierCommand(
            String min,
            String max,
            FeeStrategyType tierStrategyType,
            String tierValue
    ) {}
}
