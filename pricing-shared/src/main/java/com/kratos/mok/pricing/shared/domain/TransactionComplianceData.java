package com.kratos.mok.pricing.shared.domain;

import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.vo.Money;

public record TransactionComplianceData(
        TransactionType type,
        Money amount,
        Integer dailyCount,   // optionnel
        Integer monthlyCount  // optionnel
) {}
