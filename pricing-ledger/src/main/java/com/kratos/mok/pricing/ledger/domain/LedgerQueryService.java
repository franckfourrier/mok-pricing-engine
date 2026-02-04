package com.kratos.mok.pricing.ledger.domain;

import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.LocalDateTime;
import java.util.List;

public interface LedgerQueryService {

    Money balanceAt(String accountCode, LocalDateTime at);

    List<LedgerEntryView> lastEntries(String accountCode, int limit);

    record LedgerEntryView(
            String externalTxId,
            LocalDateTime occurredAt,
            String direction,
            Money amount,
            String kind,
            String policyId,
            String description
    ) {}
}
