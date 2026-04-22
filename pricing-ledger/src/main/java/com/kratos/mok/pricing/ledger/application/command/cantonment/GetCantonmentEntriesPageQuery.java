package com.kratos.mok.pricing.ledger.application.command.cantonment;

public record GetCantonmentEntriesPageQuery(
        int page,
        int size,
        String startDate,
        String endDate
) {}