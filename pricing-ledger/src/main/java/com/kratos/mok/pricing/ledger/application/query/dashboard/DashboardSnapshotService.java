package com.kratos.mok.pricing.ledger.application.query.dashboard;

import com.kratos.mok.pricing.ledger.infrastructure.model.DashboardSnapshotEntity;
import com.kratos.mok.pricing.ledger.infrastructure.repository.DashboardSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardSnapshotService {

    private final DashboardSnapshotRepository repo;

    public void save(DashboardView view) {

        var entities = List.of(
                toEntity(view.cant(), view),
                toEntity(view.exp(), view),
                toEntity(view.tax(), view),
                toEntity(view.dist(), view),
                toEntity(view.ext(), view)
        );

        repo.saveAll(entities);
    }

    private DashboardSnapshotEntity toEntity(BalanceView b, DashboardView v) {

        var e = new DashboardSnapshotEntity();

        e.setAccountCode(b.accountCode());
        e.setBalance(b.amount());
        e.setCurrency(b.currency());
        e.setLastVariation(b.variation());
        e.setLastTrend(b.trend());
        e.setUpdatedAt(v.updatedAt());

        return e;
    }
}