package com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto.updateCommissionPlan;

import com.kratos.mok.pricing.commissions.application.command.updateCommissionPlan.KeyCommand;
import com.kratos.mok.pricing.commissions.application.command.updateCommissionPlan.UpdateCommissionPlanCommand;

public class UpdateCommissionPlanCommandMapper {
    private UpdateCommissionPlanCommandMapper() {}

    public static UpdateCommissionPlanCommand toCommand(String id, UpdateCommissionPlanRequest req) {
        return new UpdateCommissionPlanCommand(
                id,
                req.transactionCode(),
                req.keys() == null ? null :
                        req.keys().stream()
                                .map(k -> new KeyCommand(k.beneficiary(), k.percentage()))
                                .toList()
        );
    }
}
