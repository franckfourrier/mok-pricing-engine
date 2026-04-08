package com.kratos.mok.pricing.ledger.application.query.dashboard;

import com.kratos.mok.pricing.ledger.infrastructure.model.DashboardSnapshotEntity;
import com.kratos.mok.pricing.ledger.infrastructure.repository.DashboardSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDashboardCachedQueryHandler {

    private final DashboardSnapshotRepository repo;

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

    @Cacheable(value = "dashboard", key = "'GLOBAL'")
    public DashboardView handle() {

        List<String> accounts = List.of(
                accCant, accExp, accTax, accDist, accExt
        );

        var list = repo.findAllById(accounts);

        return new DashboardView(
                find(list, accCant),
                find(list, accExp),
                find(list, accTax),
                find(list, accDist),
                find(list, accExt)

        );
    }

    private BalanceView find(List<DashboardSnapshotEntity> list, String code) {
        return list.stream()
                .filter(e -> e.getAccountCode().equals(code))
                .findFirst()
                .map(e -> new BalanceView(
                        e.getAccountCode(),
                        e.getBalance(),
                        e.getCurrency(),
                        e.getLastVariation(),
                        e.getLastTrend()
                ))
                .orElse(new BalanceView(code, BigDecimal.ZERO, "XAF", BigDecimal.ZERO, "STABLE"));
    }
}