package com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.bankDeposit;

import com.kratos.mok.pricing.app.application.command.bankDeposit.RecordBankDepositCommand;
import com.kratos.mok.pricing.shared.domain.vo.Money;

public final class BankDepositNotificationMapper {

    private BankDepositNotificationMapper() {}

    public static RecordBankDepositCommand toCommand(BankDepositNotificationRequest r) {
        return new RecordBankDepositCommand(
                r.referencePayment(),
                Money.of(r.amount(), r.currency()),
                r.superDistributor(),
                r.occurredAt()
        );
    }
}
