package com.kratos.mok.pricing.ledger.infrastructure.repository;

import com.kratos.mok.pricing.ledger.domain.LedgerAccount;
import com.kratos.mok.pricing.ledger.domain.repository.LedgerAccountRepository;
import com.kratos.mok.pricing.ledger.domain.vo.AccountCode;
import com.kratos.mok.pricing.ledger.infrastructure.mapper.LedgerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostgresLedgerAccountRepository implements LedgerAccountRepository {

    private final JpaLedgerAccountRepository jpa;
    private final LedgerMapper mapper;

    @Override
    public boolean existsByCode(AccountCode code) {
        return jpa.existsByCode(code.value());
    }

    @Override
    public Optional<LedgerAccount> findByCode(AccountCode code) {
        return jpa.findByCode(code.value()).map(mapper::toDomain);
    }

    @Override
    public void save(LedgerAccount account) {
        // V1 : utilisé surtout par bootstrap; si tu veux save domain->entity, on ajoute mapper dédié
        throw new UnsupportedOperationException("Save via bootstrap only in V1");
    }
}
