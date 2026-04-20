package com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto;

import com.kratos.mok.pricing.commissions.application.command.createCommissionPlan.CreateCommissionPlanCommand;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;

public final class CreateCommissionPlanCommandMapper {

    private CreateCommissionPlanCommandMapper() {}

    public static CreateCommissionPlanCommand toCommand(CreateCommissionPlanRequest req) {
        return new CreateCommissionPlanCommand(
                req.transactionCode(),
                req.transactionCode().transactionType(),
                TargetScope.GLOBAL,
                "ALL",
                req.keys() == null ? null :
                        req.keys().stream()
                                .map(k -> new com.kratos.mok.pricing.commissions.application.command.createCommissionPlan.KeyCommand(
                                        k.beneficiary(), k.percentage()
                                ))
                                .toList(),
                req.validityStart(),
                req.validityEnd()
        );
    }
}
