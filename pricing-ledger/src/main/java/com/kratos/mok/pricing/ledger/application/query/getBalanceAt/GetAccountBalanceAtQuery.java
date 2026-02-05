package com.kratos.mok.pricing.ledger.application.query.getBalanceAt;

import java.time.LocalDateTime;

public record GetAccountBalanceAtQuery(String accountCode, LocalDateTime at) {}
