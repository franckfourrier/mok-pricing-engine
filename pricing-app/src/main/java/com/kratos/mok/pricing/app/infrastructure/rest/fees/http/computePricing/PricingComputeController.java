package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.computePricing;

import com.kratos.mok.pricing.app.application.command.computePricing.ComputePricingBreakdownService;
import com.kratos.mok.pricing.shared.api.*;
import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pricing")
public class PricingComputeController {

    private final ComputePricingBreakdownService service;

    public PricingComputeController(ComputePricingBreakdownService service) {
        this.service = service;
    }

    @PostMapping("/compute")
    public PricingBreakdownResponse compute(@RequestBody @Valid PricingComputeRequest req) {

        TransactionType txType = TransactionType.valueOf(req.transactionType().trim().toUpperCase());
        AccountType accType = AccountType.valueOf(req.accountType().trim().toUpperCase());

        Money amount = Money.of(req.amount().amount(), req.amount().currency());

        var ctx = new PricingRequestContext(
                txType,
                amount,
                req.accountId(),
                accType,
                req.kycValidated(),
                req.monthlyTxCount(),
                req.occurredAt()
        );

        var out = service.compute(ctx);

        return new PricingBreakdownResponse(
                out.transactionType(),
                out.currency(),
                out.amount(),
                out.fee(),
                null,
                null,
                out.totalDebited(),
                null,
                new SelectedPoliciesDto(out.feePolicyId(), out.taxPolicyId(), out.commissionPolicyId())
        );
    }

    private static MoneyDto toDto(Money m) {
        return new MoneyDto(m.amount(), m.currency());
    }
}