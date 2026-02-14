package com.kratos.mok.pricing.app.application.command.applyPricingToTransaction;

import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApplyPricingToTransactionCommand(
        String externalTxId,
        TransactionType type,
        Money amount,
        String currency,
        String payerAccountId,
        AccountType payerAccountType,
        boolean kycValidated,
        int monthlyTxCount,
        OffsetDateTime occurredAt,

        Map<String, String> beneficiaryAccounts,
        boolean creditExternalAccounts
) {}
