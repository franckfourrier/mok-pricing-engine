package com.kratos.mok.pricing.ledger.application.service;

import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionCommand;
import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionCommandHandler;
import com.kratos.mok.pricing.ledger.application.command.recordTransaction.RecordLedgerTransactionResponse;
import com.kratos.mok.pricing.ledger.application.port.LedgerWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LedgerWriterImpl implements LedgerWriter {

    private final RecordLedgerTransactionCommandHandler handler;

    @Override
    public RecordLedgerTransactionResponse record(
            RecordLedgerTransactionCommand command, String actor
    ) {
        return handler.handle(command, actor);
    }
}
