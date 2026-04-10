package com.kratos.mok.pricing.ledger.application.query.dashboard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetDashboardQueryHandler {

    private final GetDashboardCachedQueryHandler cachedHandler;
    private final GetDashboardNowQueryHandler nowHandler;
    private final DashboardSnapshotService snapshotService;

    public DashboardView handle() {

        try {
            return cachedHandler.handle();

        } catch (IllegalStateException e) {
            log.warn("Dashboard cache miss → fallback realtime");
        }

        DashboardView fresh = nowHandler.handle();

        snapshotService.save(fresh);

        return fresh;
    }
}
