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
    //private final TaxComputationPort taxPort;
    //private final CommissionComputationPort commissionPort;

    public ComputePricingBreakdownQueryHandler(
            ComputeFeeQuery feeQuery
            //TaxComputationPort taxPort,
            //CommissionComputationPort commissionPort
    ) {
        this.feeQuery = feeQuery;
        //this.taxPort = taxPort;
        //this.commissionPort = commissionPort;
    }

    public PricingBreakdownResult compute(PricingRequestContext ctx) {

        var feeComputationResult = feeQuery.computeFee(ctx);
        //var tax = taxPort.computeTax(ctx);
        //var commission = commissionPort.computeCommission(ctx);

        Money totalDebited = ctx.amount().add(feeComputationResult.fee());
        //Money totalDebited = ctx.amount().plus(feeComputationResult.fee().amount()).plus(tax.amount());
        //Money totalCredited = ctx.amount().minus(commission.amount());

        /*return new PricingBreakdown(
                ctx.transactionType().name(),
                ctx.amount(),
                fee.amount(),
                tax.amount(),
                commission.amount(),
                totalDebited,
                totalCredited,
                fee.policyId(),
                tax.policyId(),
                commission.policyId()
        );*/

        return new PricingBreakdownResult(
                ctx.transactionType().name(),
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
