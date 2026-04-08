package com.kratos.mok.pricing.ledger.application.query.getBalanceAt;

import java.time.OffsetDateTime;

public record GetAccountBalanceAtQuery(String accountCode, OffsetDateTime at) {}
