package com.kratos.mok.pricing.app.application.command.bankDeposit;

import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.OffsetDateTime;

public record BankDepositCommand(
        String referencePayment,
        Money amount,
        String superDistributorId,
        OffsetDateTime occurredAt
) {}
