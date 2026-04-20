package com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.bankDeposit;

import com.kratos.mok.pricing.app.application.command.bankDeposit.BankDepositCommand;
import com.kratos.mok.pricing.shared.domain.vo.Money;

public final class BankDepositNotificationMapper {

    private BankDepositNotificationMapper() {}

    public static BankDepositCommand toCommand(BankDepositNotificationRequest r) {
        return new BankDepositCommand(
                r.referencePayment(),
                Money.of(r.amount(), r.currency()),
                r.partnerId(),
                r.occurredAt()
        );
    }
}
