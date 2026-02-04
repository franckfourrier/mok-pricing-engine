package com.kratos.mok.pricing.ledger.infrastructure.repository;

import com.kratos.mok.pricing.ledger.domain.enums.EntryDirection;
import com.kratos.mok.pricing.ledger.infrastructure.model.LedgerEntryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface JpaLedgerEntryRepository extends JpaRepository<LedgerEntryEntity, String> {

    boolean existsByExternalTxId(String externalTxId);

    List<LedgerEntryEntity> findByAccountCodeOrderByOccurredAtDesc(String accountCode, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(
          CASE WHEN e.direction = :credit THEN e.amount ELSE -e.amount END
        ), 0)
        FROM LedgerEntryEntity e
        WHERE e.accountCode = :code
          AND e.currency = :currency
          AND e.occurredAt <= :at
    """)
    BigDecimal balanceAt(
            @Param("code") String code,
            @Param("currency") String currency,
            @Param("at") LocalDateTime at,
            @Param("credit") EntryDirection credit
    );
}
