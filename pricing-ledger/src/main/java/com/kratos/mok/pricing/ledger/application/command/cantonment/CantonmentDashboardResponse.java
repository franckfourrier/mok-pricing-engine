package com.kratos.mok.pricing.ledger.application.command.cantonment;

import com.kratos.mok.pricing.shared.api.PageResponseDto;

import java.math.BigDecimal;

public record CantonmentDashboardResponse(
        BigDecimal currentBalance,
        String formattedBalance,
        String currency,
        String trend,
        PageResponseDto<CantonmentEntrySummary> history
) {}