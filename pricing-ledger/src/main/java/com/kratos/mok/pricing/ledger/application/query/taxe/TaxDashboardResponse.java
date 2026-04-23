package com.kratos.mok.pricing.ledger.application.query.taxe;

import java.math.BigDecimal;
import java.util.List;

public record TaxDashboardResponse(
        BigDecimal currentBalance,
        String formattedBalance,
        String currency,
        String trend,
        List<TaxEntrySummary> items
) {}
