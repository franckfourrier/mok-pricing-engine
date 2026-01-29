package com.kratos.mok.pricing.fees.application.query.computeFee;

import com.kratos.mok.pricing.shared.api.MoneyDto;

public record FeeComputeResponse(
        String transactionType,
        MoneyDto transactionAmount,
        MoneyDto fee,
        String feePolicyId
) {}
