package com.kratos.mok.pricing.fees.application.command.rejectFeePolicy;

public record RejectFeePolicyResponse(String policyId, boolean success, String status) {}