package com.kratos.mok.pricing.ledger.application.query.distributed;

import com.kratos.mok.pricing.ledger.application.query.dashboard.GetDashboardCachedQueryHandler;
import com.kratos.mok.pricing.shared.domain.money.MoneyFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDistributedDashboardQueryHandler {

    private final GetDashboardCachedQueryHandler cachedDashboardHandler;

    public DistributedDashboardResponse handle() {
        // 1. Récupération du snapshot global
        var dashboard = cachedDashboardHandler.handle();

        // 2. Récupération du solde global (la Card)
        var totalDist = dashboard.dist();

        // 3. Construction de la liste basée sur tes comptes YAML
        List<DistributedAccountSummary> items = List.of(
                new DistributedAccountSummary(
                        "001",
                        "Super Distributeurs",
                        dashboard.distSuper().memberCount(),
                        MoneyFormatter.format(dashboard.distSuper().amount(), dashboard.distSuper().currency())
                ),
                new DistributedAccountSummary(
                        "002",
                        "Distributeurs",
                        dashboard.distDist().memberCount(),
                        MoneyFormatter.format(dashboard.distDist().amount(), dashboard.distDist().currency())
                ),
                new DistributedAccountSummary(
                        "003",
                        "Agents",
                        dashboard.distAgent().memberCount(),
                        MoneyFormatter.format(dashboard.distAgent().amount(), dashboard.distAgent().currency())
                )
        );

        return new DistributedDashboardResponse(
                totalDist.amount(),
                MoneyFormatter.format(totalDist.amount(), totalDist.currency()),
                totalDist.currency(),
                totalDist.trend(),
                items
        );
    }
}
