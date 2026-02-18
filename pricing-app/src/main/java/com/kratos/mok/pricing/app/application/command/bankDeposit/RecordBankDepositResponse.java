package com.kratos.mok.pricing.app.application.command.bankDeposit;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record RecordBankDepositResponse(
        String referenceVersement,
        boolean recorded,
        String cantonnementAccount,
        Money amount
) {}
