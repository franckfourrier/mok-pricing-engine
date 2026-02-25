package com.kratos.mok.pricing.fees.application.command.rejecteFeePolicy;

public record RejecteFeePolicyResponse(String policyId, boolean success, String status) {}