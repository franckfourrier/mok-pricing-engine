package com.kratos.mok.pricing.ledger.infrastructure.repository;

import com.kratos.mok.pricing.ledger.domain.repository.AccountBalanceProjection;
import com.kratos.mok.pricing.ledger.infrastructure.model.LedgerEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface JpaLedgerEntryRepository extends JpaRepository<LedgerEntryEntity, String> {

    boolean existsByExternalTxId(String externalTxId);

    List<LedgerEntryEntity> findByAccountCodeOrderByOccurredAtDesc(String accountCode, Pageable pageable);

    @Query(value = """
    SELECT COALESCE(
        SUM(amount) FILTER (WHERE direction = 'CREDIT'), 0
    ) - COALESCE(
        SUM(amount) FILTER (WHERE direction = 'DEBIT'), 0
    )
    FROM ledger_entries
    WHERE account_code = :code
      AND currency = :currency
      AND occurred_at <= :at
""", nativeQuery = true)
    BigDecimal balanceAt(
            @Param("code") String code,
            @Param("currency") String currency,
            @Param("at") OffsetDateTime at
    );

    @Query("""
    SELECT 
        e.accountCode as accountCode,
        SUM(
            CASE 
                WHEN e.direction = 'CREDIT' THEN e.amount
                ELSE -e.amount
            END
        ) as balance
    FROM LedgerEntryEntity e
    WHERE e.accountCode IN :accounts
    GROUP BY e.accountCode
""")
    List<AccountBalanceProjection> computeBalances(List<String> accounts);

    Page<LedgerEntryEntity> findByAccountCode(String accountCode, Pageable pageable);
}
