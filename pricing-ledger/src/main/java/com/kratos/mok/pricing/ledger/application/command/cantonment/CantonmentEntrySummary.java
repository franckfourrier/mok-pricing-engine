package com.kratos.mok.pricing.ledger.application.command.cantonment;

public record CantonmentEntrySummary(
        String id,
        String shortId,
        String entry,
        String exit,
        String motif,
        String date,
        String hour
) {}