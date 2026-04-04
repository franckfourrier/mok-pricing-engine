package com.kratos.mok.pricing.commissions.application.query.listCommissionPlans;

import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;

import java.time.LocalDateTime;
import java.util.List;

public record CommissionPlanSummary(
        String id,
        TransactionCode transactionCode,
        String transactionCodeLabel,

        List<CommissionLineSummary> lines,

        LocalDateTime date,
        String statusLabel,
        String statusRaw
) {}
