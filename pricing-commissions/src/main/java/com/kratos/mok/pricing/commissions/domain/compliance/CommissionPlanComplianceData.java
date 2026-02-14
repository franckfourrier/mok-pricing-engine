package com.kratos.mok.pricing.commissions.domain.compliance;

import com.kratos.mok.pricing.commissions.domain.CommissionTarget;
import com.kratos.mok.pricing.commissions.domain.enums.CommissionStrategyType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.util.Map;

public record CommissionPlanComplianceData(
        TransactionType transactionType,
        CommissionTarget target,
        CommissionStrategyType strategyType,

        Map<String, BigDecimal> shares
) {}

