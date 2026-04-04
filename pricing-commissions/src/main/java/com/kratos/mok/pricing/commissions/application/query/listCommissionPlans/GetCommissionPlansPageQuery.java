package com.kratos.mok.pricing.commissions.application.query.listCommissionPlans;

import com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;

public record GetCommissionPlansPageQuery(
        int page,
        int size,
        TransactionCode transactionCode,
        CommissionPlanStatus status
) {}

/*public record GetCommissionPlansPageQuery(
        int page,
        int size,
        TransactionType transactionType,
        TargetScope targetScope,
        String targetValue,
        CommissionPlanStatus status
) {}*/
