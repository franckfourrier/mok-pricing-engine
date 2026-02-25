package com.kratos.mok.pricing.commissions.application.query.listCommissionPlans;

import com.kratos.mok.pricing.shared.domain.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public record CommissionPlanSummary(
        String id,
        TransactionType transactionType,
        String transactionTypeLabel,
        List<CommissionLineSummary> lines,
        String statusLabel,
        String statusRaw,
        LocalDateTime createdAt
) {}
