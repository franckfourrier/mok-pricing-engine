package com.kratos.mok.pricing.ledger.application.query.dashboard;

import java.time.OffsetDateTime;

public record DashboardView(
        String currency,
        OffsetDateTime updatedAt,

        BalanceView cant,
        BalanceView exp,
        BalanceView tax,
        BalanceView taxFixed,
        BalanceView taxRate,
        BalanceView dist,
        BalanceView ext
) {
}