package com.kratos.mok.pricing.app.application.query.computePricingBreakdown;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record PricingBreakdownResult(
        String transactionCode,
        String transactionType,
        Money amount,
        Money fee,
        Money tax,
        Money commission,
        Money totalDebited,
        Money totalCredited,
        String feePolicyId,
        String taxPolicyId,
        String commissionPolicyId
) {}
