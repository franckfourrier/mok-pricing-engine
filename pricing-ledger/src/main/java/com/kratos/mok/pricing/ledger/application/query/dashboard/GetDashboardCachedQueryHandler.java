package com.kratos.mok.pricing.ledger.application.query.dashboard;

import com.kratos.mok.pricing.ledger.infrastructure.model.DashboardSnapshotEntity;
import com.kratos.mok.pricing.ledger.infrastructure.repository.DashboardSnapshotRepository;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDashboardCachedQueryHandler {

    private final DashboardSnapshotRepository repo;

    private final TimeProvider timeProvider;

    @Value("${ledger.accounts.cantonment}")
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

    @Value("${ledger.accounts.distributedSuperDistributor}")
    private String accDistSuperDistributor;

    @Value("${ledger.accounts.distributedDistributor}")
    private String accDistDistributor;

    @Value("${ledger.accounts.distributedAgent}")
    private String accDistAgent;

    @Value("${ledger.accounts.external}")
    private String accExt;

    @Cacheable(value = "dashboard", key = "'GLOBAL'")
    public DashboardView handle() {

        var now = timeProvider.now();

        List<String> accounts = List.of(
                accCant, accExp, accTax, accTaxRate, accTaxFixed, accDist, accDistSuperDistributor, accDistDistributor, accDistAgent, accExt
        );

        var list = repo.findAllById(accounts);

        /*if (list.isEmpty()) {
            throw new IllegalStateException("No dashboard snapshot found");
        }*/

        var cant = find(list, accCant);
        var exp  = find(list, accExp);
        var tax  = find(list, accTax);
        var taxRate  = find(list, accTaxRate);
        var taxFixed  = find(list, accTaxFixed);
        var dist = find(list, accDist);
        var distSuper = find(list, accDistSuperDistributor);
        var distDist  = find(list, accDistDistributor);
        var distAgent = find(list, accDistAgent);
        var ext  = find(list, accExt);

        // Currency cohérente
        String currency = cant.currency();

        validateMonoCurrency(cant, exp, tax, taxFixed, taxRate, dist, distSuper, distDist, distAgent,  ext);

        // Timestamp depuis DB (pas system clock)
       /* OffsetDateTime updatedAt = list.stream()
                .map(DashboardSnapshotEntity::getUpdatedAt)
                .max(OffsetDateTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("No snapshot timestamp found"));*/

        OffsetDateTime updatedAt = list.stream()
                .map(DashboardSnapshotEntity::getUpdatedAt)
                .max(OffsetDateTime::compareTo)
                .orElse(now);

        return new DashboardView(
                currency,
                updatedAt,
                cant,
                exp,
                tax,
                taxFixed,
                taxRate,
                dist,
                distSuper,
                distDist,
                distAgent,
                ext
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
                        e.getLastTrend(),
                        e.getMemberCount()
                ))
                .orElse(new BalanceView(code, BigDecimal.ZERO, "XAF", BigDecimal.ZERO, "STABLE", 0));
    }

    // Sécurité currency
    private void validateMonoCurrency(BalanceView... views) {
        var currencies = Arrays.stream(views)
                .map(BalanceView::currency)
                .distinct()
                .toList();

        if (currencies.size() > 1) {
            throw new IllegalStateException("Multi-currency dashboard not supported: " + currencies);
        }
    }
}