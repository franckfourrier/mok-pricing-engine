package com.kratos.mok.pricing.app.application.query.computePricingBreakdown;

import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;

public interface ComputePricingBreakdownQuery {
    PricingBreakdownResult compute(PricingRequestContext ctx);
}
