package com.kratos.mok.pricing.app.infrastructure.rest.ledger.http.cantonment;

import com.kratos.mok.pricing.ledger.application.command.cantonment.CantonmentEntrySummary;
import com.kratos.mok.pricing.ledger.application.command.cantonment.GetCantonmentEntriesPageQuery;
import com.kratos.mok.pricing.ledger.application.command.cantonment.GetCantonmentEntriesPageQueryHandler;
import com.kratos.mok.pricing.shared.api.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ledger/cantonment")
public class CantonmentQueryController {

    private final GetCantonmentEntriesPageQueryHandler handler;

    public CantonmentQueryController(GetCantonmentEntriesPageQueryHandler handler) {
        this.handler = handler;
    }

    @Operation(summary = "Get cantonment entries (paginated)")
    @GetMapping
    public PageResponseDto<CantonmentEntrySummary> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        var query = new GetCantonmentEntriesPageQuery(page, size, startDate, endDate);
        return handler.handle(query);
    }
}