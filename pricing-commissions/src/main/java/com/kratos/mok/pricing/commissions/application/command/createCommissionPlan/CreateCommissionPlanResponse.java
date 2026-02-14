package com.kratos.mok.pricing.commissions.application.command.createCommissionPlan;

import com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus;

public record CreateCommissionPlanResponse(
        String commissionPlanId,
        boolean success,
        CommissionPlanStatus status
) {}
