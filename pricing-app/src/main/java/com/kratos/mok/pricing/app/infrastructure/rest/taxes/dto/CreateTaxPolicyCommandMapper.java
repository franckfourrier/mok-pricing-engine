package com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto;

import com.kratos.mok.pricing.taxes.application.command.createTaxPolicy.CreateTaxPolicyCommand;

public class CreateTaxPolicyCommandMapper {

    public static CreateTaxPolicyCommand toCommand(CreateTaxPolicyRequest r) {
        return new CreateTaxPolicyCommand(
                r.type(),
                r.targetScope(),
                r.targetValue(),
                r.currency(),
                r.mode(),
                r.strategyType(),
                r.rate(),
                r.fixedAmount(),
                r.fluxIntensity(),
                r.activationThreshold(),
                r.minTax(),
                r.maxTax(),
                r.validityStart(),
                r.validityEnd()
        );
    }
}
