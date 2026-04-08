package com.kratos.mok.pricing.app.infrastructure.rest.ledger.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        BalanceDTO cantonnement,
        BalanceDTO exploitation,
        BalanceDTO tax,
        BalanceDTO distributed,
        BalanceDTO external
) {

    public record BalanceDTO(
            String accountCode,
            BigDecimal amount,
            String currency,
            BigDecimal variation,
            String trend
    ) {}
}