package com.kratos.mok.pricing.app.infrastructure.rest.fees.dto;

import com.kratos.mok.pricing.fees.application.command.updateFeePolicy.UpdateFeePolicyCommand;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;

public final class UpdateFeePolicyCommandMapper {

    private UpdateFeePolicyCommandMapper() {}

    public static UpdateFeePolicyCommand toCommand(String policyId, UpdateFeePolicyRequest req) {
        return new UpdateFeePolicyCommand(
                policyId,
                req.transactionCode(),
                TargetScope.GLOBAL,
                "ALL",
                req.currency(),
                req.strategyType(),
                req.fixedAmount(),
                req.percentage(),
                req.tiers() == null ? null :
                        req.tiers().stream()
                                .map(t -> new UpdateFeePolicyCommand.TierCommand(
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