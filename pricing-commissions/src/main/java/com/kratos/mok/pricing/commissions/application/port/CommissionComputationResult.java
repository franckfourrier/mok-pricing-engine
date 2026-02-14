package com.kratos.mok.pricing.commissions.application.port;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record CommissionComputationResult(String commissionPlanId, Money commission) {}
