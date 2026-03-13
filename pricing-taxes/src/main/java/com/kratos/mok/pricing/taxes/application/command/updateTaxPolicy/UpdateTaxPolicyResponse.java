package com.kratos.mok.pricing.taxes.application.command.updateTaxPolicy;

public record UpdateTaxPolicyResponse(
        String policyId,
        boolean updated,
        String status
) {}