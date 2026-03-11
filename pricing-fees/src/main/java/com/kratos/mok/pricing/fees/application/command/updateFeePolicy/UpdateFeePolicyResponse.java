package com.kratos.mok.pricing.fees.application.command.updateFeePolicy;

import com.kratos.mok.pricing.fees.domain.enums.FeePolicyStatus;

public record UpdateFeePolicyResponse(
        String policyId,
        boolean success,
        FeePolicyStatus status
) {}
