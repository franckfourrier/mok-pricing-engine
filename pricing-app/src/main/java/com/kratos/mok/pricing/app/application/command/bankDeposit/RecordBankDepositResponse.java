package com.kratos.mok.pricing.app.application.command.bankDeposit;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record RecordBankDepositResponse(
        String referencePayment,
        boolean recorded,
        String status,
        String ledgerExternalTxId,
        String cantonmentAccount,
        Money amount
) {}
