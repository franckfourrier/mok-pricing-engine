package com.kratos.mok.pricing.taxes.application.command.createTaxPolicy;

public record CreateTaxPolicyResponse(
        String policyId,
        boolean created,
        String status
) {}
