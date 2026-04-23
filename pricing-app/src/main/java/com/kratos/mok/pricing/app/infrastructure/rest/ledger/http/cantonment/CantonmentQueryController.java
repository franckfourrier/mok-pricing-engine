package com.kratos.mok.pricing.app.infrastructure.rest.ledger.http.cantonment;

import com.kratos.mok.pricing.ledger.application.query.cantonment.CantonmentDashboardResponse;
import com.kratos.mok.pricing.ledger.application.query.cantonment.GetCantonmentEntriesPageQuery;
import com.kratos.mok.pricing.ledger.application.query.cantonment.GetCantonmentEntriesPageQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/v1/ledger/cantonment")
public class CantonmentQueryController {

    private final GetCantonmentEntriesPageQueryHandler handler;

    public CantonmentQueryController(GetCantonmentEntriesPageQueryHandler handler) {
        this.handler = handler;
    }

    @Operation(summary = "Get cantonment dashboard (Balance + Paginated entries)")
    @GetMapping
    public CantonmentDashboardResponse getDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OffsetDateTime startDate,
            @RequestParam(required = false) OffsetDateTime endDate
    ) {
        var query = new GetCantonmentEntriesPageQuery(page, size, startDate, endDate);
        return handler.handle(query);
    }
}