package com.kratos.mok.pricing.taxes.application.command.createTaxPolicy;

import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;

import java.util.List;

public record CreateTaxPolicyCommand(
        List<TransactionCode> transactionCodes,
        TargetScope targetScope,
        String targetValue,

        String currency,          // "XAF"
        TaxMode mode,             // CANTONNEMENT / EXPLOITATION

        TaxStrategyType strategyType,
        String rate,              // ex "0.015" si ELECTRONIC_RATE
        String fixedAmount,       // ex "50"   si FIXED_AMOUNT

        String fluxIntensity,     // optionnel, défaut "1"
        boolean exempted          // optionnel, défaut false
) {}
