package com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.externalTransfer;

import com.kratos.mok.pricing.app.application.command.externalTransfer.ExternalTransferCommand;
import com.kratos.mok.pricing.shared.domain.vo.Money;

public final class ExternalTransferNotificationMapper {

    private ExternalTransferNotificationMapper() {}

    public static ExternalTransferCommand toCommand(ExternalTransferNotificationRequest r) {
        return new ExternalTransferCommand(
                r.referencePayment(),
                Money.of(r.amount(), r.currency()),
                r.partnerId()
        );
    }
}
