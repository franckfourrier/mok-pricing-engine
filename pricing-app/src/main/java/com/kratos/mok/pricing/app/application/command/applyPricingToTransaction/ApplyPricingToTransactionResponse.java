package com.kratos.mok.pricing.app.application.command.applyPricingToTransaction;

import com.kratos.mok.pricing.commissions.application.port.ComputeCommissionDistributionQuery.CommissionDistributionResult;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.util.List;

public record ApplyPricingToTransactionResponse(
        String externalTxId,

        String feePolicyId,
        Money fee,

        String taxPolicyId,
        Money tax,

        String commissionPlanId,
        Money commissionBase,
        List<CommissionDistributionResult.Line> commissionLines,

        // traçage “payout externe”
        Money externalCommissionTotal,

        // ledger
        String ledgerExternalTxId,
        boolean recorded
) {}
