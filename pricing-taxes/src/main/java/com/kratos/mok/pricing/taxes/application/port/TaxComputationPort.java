package com.kratos.mok.pricing.taxes.application.port;

import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;

public interface TaxComputationPort {
    TaxComputationResult computeTax(PricingRequestContext ctx);
}
