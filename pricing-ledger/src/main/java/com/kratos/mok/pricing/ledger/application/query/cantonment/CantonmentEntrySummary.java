package com.kratos.mok.pricing.ledger.application.query.cantonment;

public record CantonmentEntrySummary(
        String id,
        String shortId,
        String type,
        String value,
        String motif,
        String date,
        String hour
) {}