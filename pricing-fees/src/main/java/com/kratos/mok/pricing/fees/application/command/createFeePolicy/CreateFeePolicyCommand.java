package com.kratos.mok.pricing.fees.application.command.createFeePolicy;

import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.fees.domain.enums.KycRequirement;
import com.kratos.mok.pricing.fees.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.List;

public record CreateFeePolicyCommand(
        TransactionType type,
        TargetScope targetScope,
        String targetValue,
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
