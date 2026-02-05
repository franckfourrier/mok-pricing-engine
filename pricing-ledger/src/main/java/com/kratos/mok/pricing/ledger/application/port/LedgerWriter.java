package com.kratos.mok.pricing.ledger.application.port;

import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionCommand;
import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionResponse;

public interface LedgerWriter {

    RecordLedgerTransactionResponse record(
            RecordLedgerTransactionCommand command, String actor
    );
}
