package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.compute;

import com.kratos.mok.pricing.fees.application.query.computeFee.ComputeFeeQueryHandler;
import com.kratos.mok.pricing.fees.application.query.computeFee.FeeComputeResponse;
import com.kratos.mok.pricing.shared.api.MoneyDto;
import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/v1/fees")
public class ComputeFeeQueryController {

    private final ComputeFeeQueryHandler feeService;

    public ComputeFeeQueryController(ComputeFeeQueryHandler feeService) {
        this.feeService = feeService;
    }

    @Operation(
            summary = "Compute applicable fee",
            description = "Returns the applicable fee only (no taxes, no commissions)"
    )
    @GetMapping("/compute")
    public FeeComputeResponse computeFee(
            @RequestParam String transactionCode,
            @RequestParam String amount,
            @RequestParam String currency,
            @RequestParam String accountId,
            @RequestParam String accountType,
            @RequestParam boolean kycValidated,
            @RequestParam(required = false) Integer monthlyTxCount,
            @RequestParam OffsetDateTime occurredAt
    ) {
        TransactionCode txCode = TransactionCode.valueOf(transactionCode.trim().toUpperCase());
        Money txAmount = Money.of(amount, currency);

        PricingRequestContext ctx = new PricingRequestContext(
                txCode,
                txAmount,
                accountId,
                AccountType.valueOf(accountType.trim().toUpperCase()),
                kycValidated,
                monthlyTxCount == null ? 0 : monthlyTxCount,
                occurredAt
        );

        var result = feeService.computeFee(ctx);

        return new FeeComputeResponse(
                txCode.name(),
                new MoneyDto(txAmount.amount(), txAmount.currency()),
                new MoneyDto(result.fee().amount(), result.fee().currency()),
                result.feePolicyId()
        );
    }
}