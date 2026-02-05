package com.kratos.mok.pricing.ledger.application.query.getBalanceAt;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record AccountBalanceView(String accountCode, Money balance, String currency) {}
