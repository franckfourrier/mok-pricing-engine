package com.kratos.mok.pricing.fees.application.command.createFeePolicy;

import com.kratos.mok.pricing.fees.domain.enums.FeePolicyStatus;

public record CreateFeePolicyResponse(
        String policyId,
        boolean success,
        FeePolicyStatus status
) {}
