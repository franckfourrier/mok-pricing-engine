package com.kratos.mok.pricing.app.infrastructure.rest.ledger.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public record DashboardResponse(
        String currency,
        String updatedAt,

        BalanceDTO cantonnement,
        BalanceDTO exploitation,
        BalanceDTO tax,
        BalanceDTO distributed,
        BalanceDTO external
) {

    public record BalanceDTO(
            String accountCode,
            BigDecimal amount,
            String formattedAmount,
            @JsonIgnore String currency,
            BigDecimal variation,
            String trend
    ) {}
}