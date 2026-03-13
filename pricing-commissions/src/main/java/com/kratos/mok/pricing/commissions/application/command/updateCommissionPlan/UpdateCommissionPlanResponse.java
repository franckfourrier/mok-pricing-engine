package com.kratos.mok.pricing.commissions.application.command.updateCommissionPlan;

import com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus;

public record UpdateCommissionPlanResponse(
        String commissionPlanId,
        boolean success,
        CommissionPlanStatus status
) {}