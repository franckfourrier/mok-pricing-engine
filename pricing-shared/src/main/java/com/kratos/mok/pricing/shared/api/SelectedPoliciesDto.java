package com.kratos.mok.pricing.shared.api;

public record SelectedPoliciesDto(
        String feePolicyId,
        String taxPolicyId,
        String commissionPolicyId
) {}

