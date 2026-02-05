package com.kratos.mok.pricing.app.application.command.applyPricingToTransaction;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record ApplyPricingToTransactionResponse(
        String externalTxId,
        String feePolicyId,
        Money fee,
        String taxPolicyId,
        Money tax,
        String ledgerTransactionId,
        boolean recorded
) {}