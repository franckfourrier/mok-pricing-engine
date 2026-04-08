package com.kratos.mok.pricing.ledger.application.query.dashboard;

public record DashboardView(
        BalanceView cant,
        BalanceView exp,
        BalanceView tax,
        BalanceView dist,
        BalanceView ext
) {

}
