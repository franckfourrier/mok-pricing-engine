package com.kratos.mok.pricing.app.infrastructure.rest.pricing.http.compute;

import com.kratos.mok.pricing.app.application.query.computePricing.PricingResult;
import java.math.BigDecimal;

public record ComputePricingResponse(
        String transactionCode,
        String currency,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal tax,
        BigDecimal total
) {
    public static ComputePricingResponse from(
            ComputePricingRequest req,
            PricingResult result) {

        var fee = result.fee().fee().amount();
        var tax = result.tax().tax().amount();

        BigDecimal base = new BigDecimal(req.amount());

        return new ComputePricingResponse(
                req.transactionCode(),
                req.currency(),
                base,
                fee,
                tax,
                base.add(fee).add(tax)
        );
    }
}
