package com.kratos.mok.pricing.app.application.command.applyPricingToTransaction;

import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.OffsetDateTime;

public record ApplyPricingToTransactionCommand(
        String externalTxId,
        TransactionCode transactionCode,
        Money amount,
        String currency,

        String accountId,
        AccountType accountType,
        boolean kycValidated,
        int monthlyTxCount,
        OffsetDateTime occurredAt,

        String distributorAccountId,
        String superDistributorAccountId
) {}
