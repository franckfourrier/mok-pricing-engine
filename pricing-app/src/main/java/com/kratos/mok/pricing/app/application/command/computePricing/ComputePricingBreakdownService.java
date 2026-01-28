package com.kratos.mok.pricing.app.application.command.computePricing;

import com.kratos.mok.pricing.fees.application.port.FeeComputationPort;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import org.springframework.stereotype.Service;

@Service
public class ComputePricingBreakdownService {

    private final FeeComputationPort feePort;
    //private final TaxComputationPort taxPort;
    //private final CommissionComputationPort commissionPort;

    public ComputePricingBreakdownService(
            FeeComputationPort feePort
            //TaxComputationPort taxPort,
            //CommissionComputationPort commissionPort
    ) {
        this.feePort = feePort;
        //this.taxPort = taxPort;
        //this.commissionPort = commissionPort;
    }

    public PricingBreakdown compute(PricingRequestContext ctx) {

        var feeComputationResult = feePort.computeFee(ctx);
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

        return new PricingBreakdown(
                ctx.transactionType().name(),
                ctx.amount().currency(),
                ctx.amount().amount().toPlainString(),
                feeComputationResult.fee().amount().toPlainString(),
                null,
                null,
                totalDebited.amount().toPlainString(),
                null,
                feeComputationResult.feePolicyId(),
                null,
                null
        );
    }
}
