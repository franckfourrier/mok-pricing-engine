package com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.applyPricingToTransaction;

import com.kratos.mok.pricing.app.application.command.applyPricingToTransaction.ApplyPricingToTransactionCommand;
import com.kratos.mok.pricing.shared.domain.vo.Money;

public final class ApplyPricingToTransactionCommandMapper {

    private ApplyPricingToTransactionCommandMapper() {}

    public static ApplyPricingToTransactionCommand toCommand(ApplyPricingToTransactionRequest r) {
        String currency = (r.currency() == null || r.currency().isBlank())
                ? "XAF"
                : r.currency().trim().toUpperCase();

        return new ApplyPricingToTransactionCommand(
                r.externalTxId(),
                r.transactionCode(),
                Money.of(r.amount(), currency),
                currency,
                r.accountId(),
                r.accountType(),
                r.kycValidated(),
                r.monthlyTxCount(),
                r.occurredAt(),
                r.distributorAccountId(),
                r.superDistributorAccountId()
        );
    }
}
