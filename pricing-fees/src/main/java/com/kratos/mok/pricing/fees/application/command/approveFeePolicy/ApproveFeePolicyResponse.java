package com.kratos.mok.pricing.fees.application.command.approveFeePolicy;

public record ApproveFeePolicyResponse(String policyId, boolean success, String status) {}