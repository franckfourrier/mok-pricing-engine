package com.kratos.mok.pricing.ledger.domain.repository;

import com.kratos.mok.pricing.ledger.domain.LedgerAccount;
import com.kratos.mok.pricing.ledger.domain.vo.AccountCode;

import java.util.Optional;

public interface LedgerAccountRepository {
    boolean existsByCode(AccountCode code);
    Optional<LedgerAccount> findByCode(AccountCode code);
    void save(LedgerAccount account);
}
