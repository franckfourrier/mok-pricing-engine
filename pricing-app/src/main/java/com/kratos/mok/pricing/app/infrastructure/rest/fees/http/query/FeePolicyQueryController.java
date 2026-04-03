package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.query;

import com.kratos.mok.pricing.fees.application.query.listFeePolicies.*;
import com.kratos.mok.pricing.shared.api.PageResponseDto;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/fee-policies")
public class FeePolicyQueryController {

    private final GetFeePoliciesPageQueryHandler handler;

    public FeePolicyQueryController(GetFeePoliciesPageQueryHandler handler) {
        this.handler = handler;
    }

    @Operation(summary = "List fee policies (paginated)")
    @GetMapping
    public PageResponseDto<FeePolicySummary> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,

            @RequestParam(required = false) String transactionType,
            /*@RequestParam(required = false) String targetScope,
            @RequestParam(required = false) String targetValue,*/
            @RequestParam(required = false) String status
    ) {

        TransactionType tt = (transactionType == null || transactionType.isBlank())
                ? null
                : TransactionType.valueOf(transactionType.trim().toUpperCase());

        /*TargetScope ts = (targetScope == null || targetScope.isBlank())
                ? null
                : TargetScope.valueOf(targetScope.trim().toUpperCase());

        var query = new GetFeePoliciesPageQuery(page, size, tt, ts, targetValue, status);*/
        var query = new GetFeePoliciesPageQuery(page, size, tt, status);
        return handler.handle(query);
    }
}
