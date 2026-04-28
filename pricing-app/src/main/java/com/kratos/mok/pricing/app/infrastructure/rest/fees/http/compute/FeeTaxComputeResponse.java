package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.compute;

import com.kratos.mok.pricing.shared.api.MoneyDto;

public record FeeTaxComputeResponse(
        String transactionCode,
        MoneyDto transactionAmount,
        MoneyDto fee,
        String feePolicyId,
        MoneyDto tax,
        String taxPolicyId
) {
}
