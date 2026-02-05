package com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto;

import com.kratos.mok.pricing.app.application.command.applyPricingToTransaction.ApplyPricingToTransactionCommand;
import com.kratos.mok.pricing.shared.domain.vo.Money;

public final class ApplyPricingToTransactionCommandMapper {

    private ApplyPricingToTransactionCommandMapper() {}

    public static ApplyPricingToTransactionCommand toCommand(ApplyPricingToTransactionRequest r) {
        return new ApplyPricingToTransactionCommand(
                r.externalTxId(),
                r.transactionType(),
                Money.of(r.amount(), r.currency()),
                r.currency(),
                r.accountId(),
                r.accountType(),
                r.kycValidated(),
                r.monthlyTxCount(),
                r.occurredAt()
        );
    }
}
