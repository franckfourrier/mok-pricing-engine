package com.kratos.mok.pricing.commissions.application.command.createCommissionPlan;

public record KeyCommand(
        String beneficiary, // AGENT, DISTRIBUTOR, SUPER_DISTRIBUTOR, KRATOS, MERCHANT, SUPPLIER...
        String percentage   // ex: "0.30" (=30%)  [0..1] recommandé
) {}
