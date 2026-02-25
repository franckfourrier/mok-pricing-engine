package com.kratos.mok.pricing.taxes.application.command.rejectTaxPolicy;

public record RejectTaxPolicyResponse(String policyId, boolean success, String status) {}
