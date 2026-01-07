package com.kratos.mok.pricing.fees.application.command;

public record CreateFeePolicyResponse(
        String id,
        boolean success
) {}