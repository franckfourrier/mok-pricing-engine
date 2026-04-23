package com.kratos.mok.pricing.ledger.application.query.taxe;

public record TaxEntrySummary(
        String id,
        String type,
        String generatedAmount
) {}
