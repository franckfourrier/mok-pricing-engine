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
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDashboardNowQueryHandler {

    private final GetAccountBalanceAtQueryHandler balanceHandler;
    private final GetLastEntriesQueryHandler lastEntriesHandler;
    private final TimeProvider timeProvider;

    @Value("${ledger.accounts.cantonnement:ACC-CANT}")
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

        return new DashboardView(
                compute(accCant, now),
                compute(accExp, now),
                compute(accTax, now),
                compute(accDist, now),
                compute(accExt, now)
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
}