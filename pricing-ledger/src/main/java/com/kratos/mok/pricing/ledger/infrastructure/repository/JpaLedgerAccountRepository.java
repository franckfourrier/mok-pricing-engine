package com.kratos.mok.pricing.ledger.infrastructure.repository;

import com.kratos.mok.pricing.ledger.infrastructure.model.LedgerAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaLedgerAccountRepository extends JpaRepository<LedgerAccountEntity, String> {
    boolean existsByCode(String code);
    Optional<LedgerAccountEntity> findByCode(String code);
}
