package com.kratos.mok.pricing.ledger.application.query.cantonment;

import java.time.OffsetDateTime;

public record GetCantonmentEntriesPageQuery(
        int page,
        int size,
        OffsetDateTime startDate,
        OffsetDateTime endDate
) {}