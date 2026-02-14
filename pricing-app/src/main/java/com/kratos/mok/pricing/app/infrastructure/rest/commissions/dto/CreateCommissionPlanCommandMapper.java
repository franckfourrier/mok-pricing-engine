package com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto;

import com.kratos.mok.pricing.commissions.application.command.createCommissionPlan.CreateCommissionPlanCommand;

public final class CreateCommissionPlanCommandMapper {

    private CreateCommissionPlanCommandMapper() {}

    public static CreateCommissionPlanCommand toCommand(CreateCommissionPlanRequest req) {
        return new CreateCommissionPlanCommand(
                req.type(),
                req.targetScope(),
                req.targetValue(),
                req.keys() == null ? null :
                        req.keys().stream()
                                .map(k -> new com.kratos.mok.pricing.commissions.application.command.createCommissionPlan.KeyCommand(
                                        k.beneficiary(), k.percentage()
                                ))
                                .toList(),
                req.agentPercentage(),
                req.coverageRate(),
                req.validityStart(),
                req.validityEnd()
        );
    }
}
