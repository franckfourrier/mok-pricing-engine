package com.kratos.mok.pricing.app.application.query.computePricing;

import com.kratos.mok.pricing.fees.application.port.FeeComputationResult;
import com.kratos.mok.pricing.taxes.application.port.TaxComputationResult;

public record PricingResult(
        FeeComputationResult fee,
        TaxComputationResult tax
) {}