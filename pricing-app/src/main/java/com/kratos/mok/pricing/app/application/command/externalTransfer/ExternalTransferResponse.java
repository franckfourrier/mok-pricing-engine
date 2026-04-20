package com.kratos.mok.pricing.app.application.command.externalTransfer;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record ExternalTransferResponse(
        String referencePayment,
        boolean recorded,
        String status,
        String ledgerExternalTxId,
        String cantonmentAccount,
        Money amount
) {}
