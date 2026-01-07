package com.kratos.mok.pricing.fees.application.command;

import com.kratos.mok.pricing.fees.domain.FeeLimits;
import com.kratos.mok.pricing.fees.domain.FeeTarget;
import com.kratos.mok.pricing.fees.domain.ValidityPeriod;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.strategy.FeeStrategy;
import com.kratos.mok.pricing.shared.domain.vo.Money;

public record CreateFeePolicyCommand(
        TransactionType type,
        FeeTarget target,
        FeeStrategy strategy,
        FeeLimits limits,
        Money activationThreshold,
        ValidityPeriod validity,
        boolean kycRequired,
        String authorId
) {}
