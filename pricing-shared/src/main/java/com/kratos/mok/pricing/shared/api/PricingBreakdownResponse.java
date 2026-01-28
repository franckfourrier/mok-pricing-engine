package com.kratos.mok.pricing.shared.api;

public record PricingBreakdownResponse(
        String transactionType,
        String currency,
        String amount,
        String fee,
        String tax,
        String commission,
        String totalDebited,      // exemple: amount + fee + tax
        String totalCredited,     // exemple: amount - commission
        SelectedPoliciesDto selectedPolicies
) {}
