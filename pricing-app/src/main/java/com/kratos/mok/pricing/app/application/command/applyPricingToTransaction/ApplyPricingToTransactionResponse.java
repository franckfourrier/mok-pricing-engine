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
            List<CommissionDistributionResult.Line> commissionLines,
            Money totalCommissionOut,
            boolean recorded) {

        BigDecimal feeAmount = fee != null ? fee.amount() : BigDecimal.ZERO;
        BigDecimal taxAmount = tax != null ? tax.amount() : BigDecimal.ZERO;
        String currency = fee != null ? fee.currency() : (tax != null ? tax.currency() : "XAF");

        List<PayoutDTO> payouts = commissionLines.stream()
                .map(line -> new PayoutDTO(
                        line.beneficiary(),
                        line.accountId(),
                        line.amount() != null ? line.amount().amount() : BigDecimal.ZERO
                ))
                .toList();

        return new ApplyPricingToTransactionResponse(
                txId,
                currency,
                feeAmount,
                taxAmount,
                feeAmount.add(taxAmount),
                payouts,
                totalCommissionOut != null ? totalCommissionOut.amount() : BigDecimal.ZERO,
                recorded
        );
    }
}

