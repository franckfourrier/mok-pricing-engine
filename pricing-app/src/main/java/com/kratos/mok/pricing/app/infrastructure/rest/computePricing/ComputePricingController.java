package com.kratos.mok.pricing.app.infrastructure.rest.computePricing;

import com.kratos.mok.pricing.app.application.query.computePricingBreakdown.ComputePricingBreakdownQuery;
import com.kratos.mok.pricing.app.application.query.computePricingBreakdown.PricingBreakdownResult;
import com.kratos.mok.pricing.shared.api.*;
import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pricing")
public class ComputePricingController {

    private final ComputePricingBreakdownQuery query;

    public ComputePricingController(ComputePricingBreakdownQuery query) {
        this.query = query;
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

        PricingBreakdownResult out = query.compute(ctx);

        return new PricingBreakdownResponse(
                out.transactionType(),
                toDto(out.amount()),
                toDto(out.fee()),
                null,
                null,
                toDto(out.totalDebited()),
                null,
                new SelectedPoliciesDto(out.feePolicyId(), out.taxPolicyId(), out.commissionPolicyId())
        );
    }

    private static MoneyDto toDto(Money m) {
        return new MoneyDto(m.amount(), m.currency());
    }
}