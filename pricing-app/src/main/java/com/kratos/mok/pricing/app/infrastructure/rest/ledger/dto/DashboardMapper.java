package com.kratos.mok.pricing.app.infrastructure.rest.ledger.dto;

import com.kratos.mok.pricing.ledger.application.query.dashboard.DashboardView;
import com.kratos.mok.pricing.ledger.application.query.dashboard.BalanceView;

import java.time.format.DateTimeFormatter;

public final class DashboardMapper {

    private DashboardMapper() {}

    public static DashboardResponse toResponse(DashboardView v) {
        return new DashboardResponse(
                v.currency(),
                v.updatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),

                map(v.cant()),
                map(v.exp()),
                map(v.tax()),
                map(v.dist()),
                map(v.ext())
        );
    }

    private static DashboardResponse.BalanceDTO map(BalanceView v) {
        return new DashboardResponse.BalanceDTO(
                v.accountCode(),
                v.amount(),
                v.currency(),
                v.variation(),
                v.trend()
        );
    }
}