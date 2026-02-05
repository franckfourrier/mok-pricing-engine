package com.kratos.mok.pricing.taxes.application.port;

import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;

public record TaxComputationResult(
        String taxPolicyId,
        Money tax,
        TaxMode taxMode,
        TaxStrategyType strategyType
) {}
