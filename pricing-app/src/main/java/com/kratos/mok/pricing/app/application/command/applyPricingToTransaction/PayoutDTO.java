package com.kratos.mok.pricing.app.application.command.applyPricingToTransaction;

import java.math.BigDecimal;

public record PayoutDTO(
        String beneficiary,
        String accountId,
        BigDecimal amount
) {}
