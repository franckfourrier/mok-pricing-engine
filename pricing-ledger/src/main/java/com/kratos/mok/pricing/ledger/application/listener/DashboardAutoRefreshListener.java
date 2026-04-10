package com.kratos.mok.pricing.ledger.application.listener;

import com.kratos.mok.pricing.ledger.application.query.dashboard.DashboardSnapshotUpdater;
import com.kratos.mok.pricing.ledger.domain.event.LedgerEntriesCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardAutoRefreshListener {

    private final DashboardSnapshotUpdater updater;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLedgerUpdated(LedgerEntriesCreatedEvent event) {

        log.info("Refreshing dashboard snapshot after tx={}", event.externalTxId());

        try {
            updater.refreshSnapshot();

        } catch (Exception e) {
            log.error("Dashboard refresh failed after ledger event", e);
        }
    }
}