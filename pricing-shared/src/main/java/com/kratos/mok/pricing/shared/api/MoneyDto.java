package com.kratos.mok.pricing.shared.api;

import java.math.BigDecimal;

public record MoneyDto(
        BigDecimal amount,
        String currency
) {
}
