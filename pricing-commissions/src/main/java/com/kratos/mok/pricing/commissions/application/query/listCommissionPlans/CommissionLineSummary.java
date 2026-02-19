package com.kratos.mok.pricing.commissions.application.query.listCommissionPlans;

public record CommissionLineSummary(
        String beneficiaryCode,   // SUPER_DISTRIBUTOR / DISTRIBUTOR / AGENT
        String beneficiaryLabel,  // "Super Distributeur" / "Distributeur" / "Agent"
        String valueLabel         // "10% des frais de retrait (potentiel)" etc.
) {}
