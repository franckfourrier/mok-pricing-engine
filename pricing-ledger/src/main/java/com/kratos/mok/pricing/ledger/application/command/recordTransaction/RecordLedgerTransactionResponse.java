package com.kratos.mok.pricing.ledger.application.command.recordTransaction;

public record RecordLedgerTransactionResponse(String externalTxId, boolean recorded) {}

