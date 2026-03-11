package com.kratos.mok.pricing.fees.application.command.archiveFeePolicy;

public record ArchiveFeePolicyResponse(String policyId, boolean success, String status) {}