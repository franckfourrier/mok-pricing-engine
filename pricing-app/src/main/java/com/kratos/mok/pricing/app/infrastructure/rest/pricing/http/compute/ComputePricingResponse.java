package com.kratos.mok.pricing.app.infrastructure.rest.pricing.http.compute;

import com.kratos.mok.pricing.fees.application.port.FeeComputationResult;
import com.kratos.mok.pricing.taxes.application.port.TaxComputationResult;

import java.math.BigDecimal;

public record ComputePricingResponse(
        String transactionCode,
        String currency,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal tax
) {
    public static ComputePricingResponse from(
            ComputePricingRequest req,
            FeeComputationResult feeRes,
            TaxComputationResult taxRes) {

        BigDecimal base = new BigDecimal(req.amount());
        BigDecimal f = feeRes.fee().amount();
        BigDecimal t = taxRes.tax().amount();

        return new ComputePricingResponse(
                req.transactionCode(),
                req.currency(),
                base,
                f,
                t
        );
    }
}