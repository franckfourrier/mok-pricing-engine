package com.kratos.mok.pricing.ledger.infrastructure.repository;

import com.kratos.mok.pricing.ledger.domain.LedgerEntry;
import com.kratos.mok.pricing.ledger.domain.enums.EntryDirection;
import com.kratos.mok.pricing.ledger.domain.repository.LedgerEntryRepository;
import com.kratos.mok.pricing.ledger.domain.vo.AccountCode;
import com.kratos.mok.pricing.ledger.infrastructure.mapper.LedgerMapper;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostgresLedgerEntryRepository implements LedgerEntryRepository {

    private final JpaLedgerEntryRepository jpa;
    private final LedgerMapper mapper;

    @Override
    public boolean existsByExternalTxId(String externalTxId) {
        return jpa.existsByExternalTxId(externalTxId);
    }

    @Override
    public void appendAll(List<LedgerEntry> entries) {
        jpa.saveAll(entries.stream().map(mapper::toEntity).toList());
    }

    @Override
    public Money balanceAt(AccountCode code, LocalDateTime at, String currency) {
        BigDecimal bal = jpa.balanceAt(code.value(), currency.toUpperCase(), at, EntryDirection.CREDIT);
        return Money.of(bal, currency);
    }

    @Override
    public List<LedgerEntry> lastEntries(AccountCode code, int limit) {
        return jpa.findByAccountCodeOrderByOccurredAtDesc(code.value(), PageRequest.of(0, limit))
                .stream().map(mapper::toDomain).toList();
    }
}
