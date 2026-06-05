package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.query;

import com.kratos.mok.pricing.app.infrastructure.rest.fees.dto.FeePolicySearchCriteria;
import com.kratos.mok.pricing.fees.application.query.listFeePolicies.*;
import com.kratos.mok.pricing.shared.api.BaseQueryController;
import com.kratos.mok.pricing.shared.api.PageResponseDto;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/fee-policies")
@RequiredArgsConstructor
public class FeePolicyQueryController extends BaseQueryController {

    private final GetFeePoliciesPageQueryHandler handler;

    @Operation(summary = "List fee policies (paginated)")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PageResponseDto<FeePolicySummary> list(
            @RequestParam(required = false) String transactionCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {

        var criteria = new FeePolicySearchCriteria(
                parseEnumSafe(transactionCode, TransactionCode.class, "transactionCode"),
                status
        );

        var query = new GetFeePoliciesPageQuery(
                normalizePage(page),
                normalizeSize(size),
                criteria.transactionCode(),
                criteria.status()
        );

        return handler.handle(query);
    }
}

