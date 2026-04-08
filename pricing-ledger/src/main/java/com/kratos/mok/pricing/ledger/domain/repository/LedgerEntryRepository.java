package com.kratos.mok.pricing.ledger.domain.repository;

import com.kratos.mok.pricing.ledger.domain.LedgerEntry;
import com.kratos.mok.pricing.ledger.domain.vo.AccountCode;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public interface LedgerEntryRepository {

    boolean existsByExternalTxId(String externalTxId);

    void appendAll(List<LedgerEntry> entries);

    Money balanceAt(AccountCode code, OffsetDateTime at, String currency);

    List<LedgerEntry> lastEntries(AccountCode code, int limit);

    List<AccountBalanceProjection> computeBalances(List<String> accounts);
}
