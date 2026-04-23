package com.kratos.mok.pricing.app.infrastructure.rest.ledger.http.distributed;

import com.kratos.mok.pricing.ledger.application.query.distributed.GetDistributedDashboardQueryHandler;
import com.kratos.mok.pricing.ledger.application.query.distributed.DistributedDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ledger/distributed")
@RequiredArgsConstructor
public class DistributedQueryController {

    private final GetDistributedDashboardQueryHandler handler;

    @Operation(summary = "Get distributed balance dashboard (Hierarchy breakdown)")
    @GetMapping
    public DistributedDashboardResponse getDashboard() {
        return handler.handle();
    }
}