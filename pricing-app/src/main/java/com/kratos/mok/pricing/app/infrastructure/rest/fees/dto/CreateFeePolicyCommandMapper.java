package com.kratos.mok.pricing.app.infrastructure.rest.fees.dto;

import com.kratos.mok.pricing.fees.application.command.createFeePolicy.CreateFeePolicyCommand;

public final class CreateFeePolicyCommandMapper {

    private CreateFeePolicyCommandMapper() {}

    public static CreateFeePolicyCommand toCommand(CreateFeePolicyRequest req) {
        return new CreateFeePolicyCommand(
                req.type(),
                req.targetScope(),
                req.targetValue(),
                req.currency(),
                req.strategyType(),
                req.fixedAmount(),
                req.percentage(),
                req.tiers() == null ? null :
                        req.tiers().stream()
                                .map(t -> new CreateFeePolicyCommand.TierCommand(
                                        t.min(), t.max(), t.tierStrategyType(), t.tierValue()
                                ))
                                .toList(),
                req.activationThreshold(),
                req.minFee(),
                req.maxFee(),
                req.validityStart(),
                req.validityEnd(),
                req.kycRequirement(),
                req.minMonthlyTxCount()
        );
    }
}
