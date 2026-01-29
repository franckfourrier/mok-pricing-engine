package com.kratos.mok.pricing.fees.application.port;

import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;

public interface ComputeFeeQuery {
    FeeComputationResult computeFee(PricingRequestContext ctx);
}
