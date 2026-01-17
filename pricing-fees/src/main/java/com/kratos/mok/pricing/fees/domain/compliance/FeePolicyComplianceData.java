package com.kratos.mok.pricing.fees.domain.compliance;

import com.kratos.mok.pricing.fees.domain.FeeTarget;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.shared.domain.vo.Money;

public record FeePolicyComplianceData(
        TransactionType transactionType,
        FeeTarget feeTarget,
        FeeStrategyType strategyType,
        Money minAmount,
        Money maxAmount,
        Money activationThreshold,
        boolean kycRequired
) {}

