package com.kratos.mok.pricing.fees.application.port;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record FeeComputationResult(
        String feePolicyId,
        Money fee
) {}
