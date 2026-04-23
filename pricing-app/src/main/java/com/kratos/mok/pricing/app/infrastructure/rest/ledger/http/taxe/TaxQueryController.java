package com.kratos.mok.pricing.app.infrastructure.rest.ledger.http.taxe;

import com.kratos.mok.pricing.ledger.application.query.taxe.GetTaxDashboardQueryHandler;
import com.kratos.mok.pricing.ledger.application.query.taxe.TaxDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ledger/tax")
@RequiredArgsConstructor
public class TaxQueryController {

    private final GetTaxDashboardQueryHandler handler;

    @Operation(summary = "Get tax dashboard (Global balance + Fixed/Rate sub-accounts)")
    @GetMapping
    public TaxDashboardResponse getTaxDashboard() {
        // Pas de pagination ni de filtres ici car on exploite le DashboardSnapshot (Cache)
        return handler.handle();
    }
}