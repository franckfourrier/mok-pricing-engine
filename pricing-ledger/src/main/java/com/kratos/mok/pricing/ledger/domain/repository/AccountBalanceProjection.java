package com.kratos.mok.pricing.ledger.domain.repository;

import java.math.BigDecimal;

public interface AccountBalanceProjection {
    String getAccountCode();
    BigDecimal getBalance();
}
