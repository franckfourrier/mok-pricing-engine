package com.kratos.mok.pricing.taxes.application.command.updateTaxPolicy;

import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;

import java.util.List;

public record UpdateTaxPolicyCommand(
        String policyId,
        List<TransactionCode> transactionCodes,
        TaxMode mode,
        TaxStrategyType strategyType,
        String rate,
        String fixedAmount,
        String fluxIntensity,
        boolean exempted
) {}
