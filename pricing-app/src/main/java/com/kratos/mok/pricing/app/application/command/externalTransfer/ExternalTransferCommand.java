package com.kratos.mok.pricing.app.application.command.externalTransfer;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record ExternalTransferCommand(
        String referencePayment,
        Money amount,
        String partnerId
) {}
