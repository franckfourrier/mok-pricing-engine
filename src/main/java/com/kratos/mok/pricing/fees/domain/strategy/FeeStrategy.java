package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public sealed interface FeeStrategy permits FixedFee, ProportionalFee, TieredFee {
    Money apply(Money transactionAmount);
}

