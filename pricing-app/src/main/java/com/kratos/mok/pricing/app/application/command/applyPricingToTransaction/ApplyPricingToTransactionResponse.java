package com.kratos.mok.pricing.app.application.command.applyPricingToTransaction;

import com.kratos.mok.pricing.commissions.application.port.ComputeCommissionDistributionQuery.CommissionDistributionResult;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.math.BigDecimal;
import java.util.List;

public record ApplyPricingToTransactionResponse(
        String externalTxId,
        String currency,

        BigDecimal serviceFee,
        BigDecimal taxAmount,
        BigDecimal totalDeducted,

        List<PayoutDTO> payouts,

        BigDecimal totalCommissionOut,
        boolean recorded
) {
    public static ApplyPricingToTransactionResponse fromDomain(
            String txId,
            Money fee,
            Money tax,
            List<CommissionDistributionResult.Line> lines,
            Money totalCom,
            boolean recorded) {

        return new ApplyPricingToTransactionResponse(
                txId,
                fee.currency(),
                fee.amount(),
                tax.amount(),
                fee.amount().add(tax.amount()),
                lines.stream().map(l -> new PayoutDTO(l.beneficiary(), l.accountId(), l.amount().amount())).toList(),
                totalCom.amount(),
                recorded
        );
    }
}

