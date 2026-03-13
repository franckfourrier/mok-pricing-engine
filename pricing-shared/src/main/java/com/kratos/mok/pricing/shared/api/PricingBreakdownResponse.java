package com.kratos.mok.pricing.shared.api;

public record PricingBreakdownResponse(
        String transactionCode,
        String transactionType,
        MoneyDto amount,
        MoneyDto fee,
        MoneyDto tax,
        MoneyDto commission,
        MoneyDto totalDebited,      // exemple: amount + fee + tax
        MoneyDto totalCredited,     // exemple: amount - commission
        SelectedPoliciesDto selectedPolicies
) {}
