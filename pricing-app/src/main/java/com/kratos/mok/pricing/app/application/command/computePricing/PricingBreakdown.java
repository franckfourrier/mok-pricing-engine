package com.kratos.mok.pricing.app.application.command.computePricing;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record PricingBreakdown(
        String transactionType,
        String currency,
        String amount,
        String fee,
        String tax,
        String commission,
        String totalDebited,
        String totalCredited,
        String feePolicyId,
        String taxPolicyId,
        String commissionPolicyId
) {}
