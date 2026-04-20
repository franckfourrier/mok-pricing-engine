package com.kratos.mok.pricing.commissions.application.command.updateCommissionPlan;

import java.util.List;

public record UpdateCommissionPlanCommand(
        String commissionPlanId,
        List<KeyCommand> keys
) {}