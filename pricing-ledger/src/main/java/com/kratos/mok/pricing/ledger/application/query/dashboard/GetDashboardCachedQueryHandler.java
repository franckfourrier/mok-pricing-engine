package com.kratos.mok.pricing.ledger.application.query.dashboard;

import com.kratos.mok.pricing.ledger.infrastructure.model.DashboardSnapshotEntity;
import com.kratos.mok.pricing.ledger.infrastructure.repository.DashboardSnapshotRepository;
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

    @Value("${ledger.accounts.cantonment}")
    private String accCant;

    @Value("${ledger.accounts.exploitation}")
    private String accExp;

    @Value("${ledger.accounts.tax}")
    private String accTax;

    @Value("${ledger.accounts.distributed}")
    private String accDist;

    @Value("${ledger.accounts.external}")
    private String accExt;

    @Cacheable(value = "dashboard", key = "'GLOBAL'")
    public DashboardView handle() {

        List<String> accounts = List.of(
                accCant, accExp, accTax, accDist, accExt
        );

        var list = repo.findAllById(accounts);

        var cant = find(list, accCant);
        var exp  = find(list, accExp);
        var tax  = find(list, accTax);
        var dist = find(list, accDist);
        var ext  = find(list, accExt);

        // Currency cohérente
        String currency = cant.currency();

        validateMonoCurrency(cant, exp, tax, dist, ext);

        // Timestamp depuis DB (pas system clock)
        OffsetDateTime updatedAt = list.stream()
                .map(DashboardSnapshotEntity::getUpdatedAt)
                .max(OffsetDateTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("No snapshot timestamp found"));

        return new DashboardView(
                currency,
                updatedAt,
                cant,
                exp,
                tax,
                dist,
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
                        e.getLastTrend()
                ))
                .orElse(new BalanceView(code, BigDecimal.ZERO, "XAF", BigDecimal.ZERO, "STABLE"));
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