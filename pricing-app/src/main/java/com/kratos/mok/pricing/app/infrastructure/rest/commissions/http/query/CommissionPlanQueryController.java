package com.kratos.mok.pricing.app.infrastructure.rest.commissions.http.query;

import com.kratos.mok.pricing.commissions.application.query.listCommissionPlans.*;
import com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus;
import com.kratos.mok.pricing.shared.api.PageResponseDto;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/commission-plans")
public class CommissionPlanQueryController {

    private final GetCommissionPlansPageQueryHandler handler;

    public CommissionPlanQueryController(GetCommissionPlansPageQueryHandler handler) {
        this.handler = handler;
    }

    @Operation(summary = "List commission plans (paginated)")
    @GetMapping
    public PageResponseDto<CommissionPlanSummary> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String targetScope,
            @RequestParam(required = false) String targetValue,
            @RequestParam(required = false) String status
    ) {
        TransactionType tt = (transactionType == null || transactionType.isBlank())
                ? null
                : TransactionType.valueOf(transactionType.trim().toUpperCase());

        TargetScope ts = (targetScope == null || targetScope.isBlank())
                ? null
                : TargetScope.valueOf(targetScope.trim().toUpperCase());

        CommissionPlanStatus st = (status == null || status.isBlank())
                ? null
                : CommissionPlanStatus.valueOf(status.trim().toUpperCase());

        var q = new GetCommissionPlansPageQuery(page, size, tt, ts, targetValue, st);
        return handler.handle(q);
    }
}