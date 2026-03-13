package com.kratos.mok.pricing.app.application.query.computePricingBreakdown;

import com.kratos.mok.pricing.fees.application.port.ComputeFeeQuery;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ComputePricingBreakdownQueryHandler implements ComputePricingBreakdownQuery {

    private final ComputeFeeQuery feeQuery;

    public ComputePricingBreakdownQueryHandler(ComputeFeeQuery feeQuery) {
        this.feeQuery = feeQuery;
    }

    @Override
    public PricingBreakdownResult compute(PricingRequestContext ctx) {

        var feeComputationResult = feeQuery.computeFee(ctx);

        Money totalDebited = ctx.amount().add(feeComputationResult.fee());

        return new PricingBreakdownResult(
                ctx.transactionCode().name(),
                ctx.transactionCode().transactionType().name(),
                ctx.amount(),
                feeComputationResult.fee(),
                null,
                null,
                totalDebited,
                null,
                feeComputationResult.feePolicyId(),
                null,
                null
        );
    }
}