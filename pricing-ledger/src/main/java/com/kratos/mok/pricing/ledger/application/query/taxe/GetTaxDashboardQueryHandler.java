package com.kratos.mok.pricing.ledger.application.query.taxe;

import com.kratos.mok.pricing.ledger.application.query.dashboard.GetDashboardCachedQueryHandler;
import com.kratos.mok.pricing.shared.domain.money.MoneyFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class GetTaxDashboardQueryHandler {

    private final GetDashboardCachedQueryHandler cachedDashboardHandler;

    public TaxDashboardResponse handle() {
        // 1. Récupération du snapshot global (Cache)
        var dashboard = cachedDashboardHandler.handle();

        // 2. Extraction des deux sous-comptes spécifiques
        var rateTax = dashboard.taxRate();
        var fixedTax = dashboard.taxFixed();

        // 3. Mapping vers la liste (Tableau non paginé)
        List<TaxEntrySummary> items = List.of(
                new TaxEntrySummary(
                        "001",
                        "Taxe Forfaitaire",
                        MoneyFormatter.format(fixedTax.amount(), fixedTax.currency())
                ),
                new TaxEntrySummary(
                        "002",
                        "Taxe Electronique",
                        MoneyFormatter.format(rateTax.amount(), rateTax.currency())
                )
        );

        // 4. Le solde total affiché dans la "Card" est le compte 'tax' global
        var globalTax = dashboard.tax();

        return new TaxDashboardResponse(
                globalTax.amount(),
                MoneyFormatter.format(globalTax.amount(), globalTax.currency()),
                globalTax.currency(),
                globalTax.trend(),
                items
        );
    }
}