package com.kratos.mok.pricing.ledger.application.query.dashboard;

import com.kratos.mok.pricing.ledger.application.query.getBalanceAt.*;
import com.kratos.mok.pricing.ledger.application.query.getLastEntries.*;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDashboardNowQueryHandler {

    private final GetAccountBalanceAtQueryHandler balanceHandler;
    private final GetLastEntriesQueryHandler lastEntriesHandler;
    private final TimeProvider timeProvider;

    @Value("${ledger.accounts.cantonment:ACC-CANT}")
    private String accCant;

    @Value("${ledger.accounts.exploitation:ACC-EXP}")
    private String accExp;

    @Value("${ledger.accounts.tax:ACC-TAX}")
    private String accTax;

    @Value("${ledger.accounts.distributed:ACC-DIST}")
    private String accDist;

    @Value("${ledger.accounts.external:ACC-EXT}")
    private String accExt;

    public DashboardView handle() {

        OffsetDateTime now = timeProvider.now();

        var cantView = compute(accCant, now);
        var expView = compute(accExp, now);
        var taxView = compute(accTax, now);
        var distView = compute(accDist, now);
        var extView = compute(accExt, now);

        validateMonoCurrency(cantView, expView, taxView, distView, extView);

        String globalCurrency = cantView.currency();

        return new DashboardView(
                globalCurrency,
                now,
                cantView,
                expView,
                taxView,
                distView,
                extView
        );
    }

    private BalanceView compute(String account, OffsetDateTime now) {

        var current = balanceHandler.handle(new GetAccountBalanceAtQuery(account, now));

        var lastEntries = lastEntriesHandler.handle(
                new GetLastEntriesQuery(account, 1)
        );

        BigDecimal variation = BigDecimal.ZERO;
        String trend = "STABLE";

        if (!lastEntries.isEmpty()) {
            var last = lastEntries.get(0);

            variation = last.amount().amount();

            trend = switch (last.direction()) {
                case "DEBIT" -> "DOWN";
                case "CREDIT" -> "UP";
                default -> "STABLE";
            };
        }

        return new BalanceView(
                current.accountCode(),
                current.balance().amount(),
                current.currency(),
                variation,
                trend
        );
    }

    private void validateMonoCurrency(BalanceView... views) {
        var currencies = Arrays.stream(views)
                .map(BalanceView::currency)
                .distinct()
                .toList();

        if (currencies.size() > 1) {
            throw new IllegalStateException(
                    "Multi-currency dashboard not supported: " + currencies
            );
        }
    }
}