package com.kratos.mok.pricing.ledger.application.query.dashboard;

import com.kratos.mok.pricing.ledger.domain.repository.LedgerEntryRepository;
import com.kratos.mok.pricing.ledger.infrastructure.model.DashboardSnapshotEntity;
import com.kratos.mok.pricing.ledger.infrastructure.repository.DashboardSnapshotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardSnapshotUpdater {

    private final LedgerEntryRepository entryRepo;
    private final DashboardSnapshotRepository snapshotRepo;

    @Value("${ledger.accounts.cantonnement}")
    private String accCant;

    @Value("${ledger.accounts.exploitation}")
    private String accExp;

    @Value("${ledger.accounts.tax}")
    private String accTax;

    @Value("${ledger.accounts.taxRate}")
    private String accTaxRate;

    @Value("${ledger.accounts.taxFixed}")
    private String accTaxFixed;

    @Value("${ledger.accounts.distributed}")
    private String accDist;

    @Value("${ledger.accounts.external}")
    private String accExt;

    @Value("${ledger.accounts.bankClearing}")
    private String accBankClear;

    @Transactional
    @CacheEvict(value = "dashboard", allEntries = true)
    public void refreshSnapshot() {

        List<String> accounts = List.of(
                accCant, accExp, accTax, accDist, accExt
        );

        var balances = entryRepo.computeBalances(accounts);

        LocalDateTime now = LocalDateTime.now();

        List<DashboardSnapshotEntity> toSave = balances.stream().map(b -> {

            DashboardSnapshotEntity entity = snapshotRepo
                    .findById(b.getAccountCode())
                    .orElseGet(DashboardSnapshotEntity::new);

            BigDecimal previous = entity.getBalance() != null
                    ? entity.getBalance()
                    : BigDecimal.ZERO;

            BigDecimal current = b.getBalance() != null
                    ? b.getBalance()
                    : BigDecimal.ZERO;

            BigDecimal variation = current.subtract(previous);

            entity.setAccountCode(b.getAccountCode());
            entity.setBalance(current);
            entity.setCurrency("XAF"); // OK si mono-devise
            entity.setLastVariation(variation);
            entity.setLastTrend(resolveTrend(variation));
            entity.setUpdatedAt(now);

            return entity;

        }).toList();

        snapshotRepo.saveAll(toSave); // ✅ PERF
    }

    private String resolveTrend(BigDecimal variation) {
        if (variation == null) return "STABLE";
        int sign = variation.signum();
        if (sign > 0) return "UP";
        if (sign < 0) return "DOWN";
        return "STABLE";
    }
}