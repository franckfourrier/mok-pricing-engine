package com.kratos.mok.pricing.app.application.command.externalTransfer;

import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.OffsetDateTime;

public record ExternalTransferCommand(
    String referencePayment,
    Money amount,
    String partnerId,
    OffsetDateTime occurredAt
) {
}
