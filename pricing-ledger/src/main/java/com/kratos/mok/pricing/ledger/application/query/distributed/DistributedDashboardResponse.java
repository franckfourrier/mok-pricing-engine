package com.kratos.mok.pricing.ledger.application.query.distributed;

import java.math.BigDecimal;
import java.util.List;

public record DistributedDashboardResponse(
        BigDecimal currentBalance,
        String formattedBalance,
        String currency,
        String trend,
        List<DistributedAccountSummary> items
) {}