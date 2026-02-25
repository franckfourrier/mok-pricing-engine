package com.kratos.mok.pricing.fees.application.command.rejecteFeePolicy;

public record RejectFeePolicyResponse(String policyId, boolean success, String status) {}