package com.kratos.mok.pricing.app.infrastructure.rest.taxes.http.query;

import com.kratos.mok.pricing.shared.api.PageResponseDto;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.taxes.application.query.listTaxPolicies.GetTaxPoliciesPageQuery;
import com.kratos.mok.pricing.taxes.application.query.listTaxPolicies.GetTaxPoliciesPageQueryHandler;
import com.kratos.mok.pricing.taxes.application.query.listTaxPolicies.TaxPolicySummary;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tax-policies")
public class TaxPolicyQueryController {

    private final GetTaxPoliciesPageQueryHandler handler;

    public TaxPolicyQueryController(GetTaxPoliciesPageQueryHandler handler) {
        this.handler = handler;
    }

    @Operation(summary = "List tax policies (paginated)")
    @GetMapping
    public PageResponseDto<TaxPolicySummary> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String targetScope,
            @RequestParam(required = false) String targetValue,
            @RequestParam(required = false) String status
    ) {
        TargetScope ts = (targetScope == null || targetScope.isBlank())
                ? null
                : TargetScope.valueOf(targetScope.trim().toUpperCase());

        var query = new GetTaxPoliciesPageQuery(page, size, ts, targetValue, status);
        return handler.handle(query);
    }
}