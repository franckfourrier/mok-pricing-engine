package com.kratos.mok.pricing.fees.application.adapter;

import com.kratos.mok.pricing.fees.domain.TransactionContext;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;

import java.time.LocalDateTime;

public final class FeeTransactionContextAdapter {

    private FeeTransactionContextAdapter() {}

    public static TransactionContext from(PricingRequestContext ctx) {
        return new TransactionContext(
                ctx.amount(),
                ctx.occurredAt() == null ? LocalDateTime.now() : ctx.occurredAt().toLocalDateTime(),
                ctx.accountId(),
                ctx.accountType(),
                ctx.kycValidated(),
                ctx.monthlyTxCount()
        );
    }
}
