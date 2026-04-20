package com.kratos.mok.pricing.fees.application.command.createFeePolicy;

import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.fees.domain.enums.KycRequirement;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateFeePolicyCommand(
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
        OffsetDateTime validityStart,
        OffsetDateTime validityEnd,
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
