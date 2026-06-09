package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.compute;

import com.kratos.mok.pricing.fees.application.query.computeFee.ComputeFeeQueryHandler;
import com.kratos.mok.pricing.shared.api.MoneyDto;
import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import com.kratos.mok.pricing.taxes.application.query.computeTax.ComputeTaxQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/v1/fees")
public class ComputeFeeTaxQueryController {

    private final ComputeFeeQueryHandler feeService;
    private final ComputeTaxQueryHandler taxService;

    public ComputeFeeTaxQueryController(ComputeFeeQueryHandler feeService, ComputeTaxQueryHandler taxService) {
        this.feeService = feeService;
        this.taxService = taxService;
    }

    @Operation(
            summary = "Compute applicable fee and tax",
            description = "Returns the applicable fee and tax only (no commissions)"
    )
    @GetMapping("/compute")
    public FeeTaxComputeResponse computeFee(
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

        var resultFee = feeService.computeFee(ctx);

        var resultTax = taxService.computeTax(ctx);

        return new FeeTaxComputeResponse(
                txCode.name(),
                new MoneyDto(txAmount.amount(), txAmount.currency()),
                new MoneyDto(resultFee.fee().amount(), resultFee.fee().currency()),
                resultFee.feePolicyId(),
                new MoneyDto(
                        resultTax.totalTax().amount(),
                        resultTax.totalTax().currency()
                ),
                null
        );
    }
}