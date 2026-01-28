package com.kratos.mok.pricing.commissions.application.port;

import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;

public interface CommissionComputationPort {
    CommissionComputationResult computeCommission(PricingRequestContext ctx);
}

