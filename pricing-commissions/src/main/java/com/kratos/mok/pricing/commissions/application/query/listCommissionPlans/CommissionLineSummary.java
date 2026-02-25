package com.kratos.mok.pricing.commissions.application.query.listCommissionPlans;

public record CommissionLineSummary(
        String beneficiaryCode,
        String beneficiaryLabel,
        String valueLabel
) {}
