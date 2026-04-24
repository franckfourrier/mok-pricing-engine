package com.kratos.mok.pricing.app.application.query.computePricing;

import com.kratos.mok.pricing.fees.application.query.computeFee.ComputeFeeQueryHandler;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import com.kratos.mok.pricing.taxes.application.query.computeTax.ComputeTaxQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComputePricingQueryHandler {

    private final ComputeFeeQueryHandler feeService;
    private final ComputeTaxQueryHandler taxService;

    public PricingResult handle(PricingRequestContext ctx) {

        var fee = feeService.computeFee(ctx);
        var tax = taxService.computeTax(ctx);

        return new PricingResult(fee, tax);
    }
}