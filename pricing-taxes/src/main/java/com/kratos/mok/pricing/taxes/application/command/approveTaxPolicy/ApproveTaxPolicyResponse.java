package com.kratos.mok.pricing.taxes.application.command.approveTaxPolicy;

public record ApproveTaxPolicyResponse(String policyId, boolean success, String status) {}
