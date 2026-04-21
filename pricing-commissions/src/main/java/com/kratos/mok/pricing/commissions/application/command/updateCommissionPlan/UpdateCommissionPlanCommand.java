package com.kratos.mok.pricing.commissions.application.command.updateCommissionPlan;

import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;

import java.util.List;

public record UpdateCommissionPlanCommand(
        String commissionPlanId,
        TransactionCode transactionCode,
        List<KeyCommand> keys
) {}