package com.kratos.mok.pricing.ledger.application.query.dashboard;

import java.math.BigDecimal;

public record BalanceView(
        String accountCode,
        BigDecimal amount,
        String currency,
        BigDecimal variation,
        String trend,
        long memberCount
) {
}
