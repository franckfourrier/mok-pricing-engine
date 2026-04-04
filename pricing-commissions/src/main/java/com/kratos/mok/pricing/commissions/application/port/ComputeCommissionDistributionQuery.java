package com.kratos.mok.pricing.commissions.application.port;

import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;

import java.util.List;

public interface ComputeCommissionDistributionQuery {
    CommissionDistributionResult compute(PricingRequestContext ctx, Money commissionBase);

    record CommissionDistributionResult(String commissionPlanId, List<Line> lines) {
        public record Line(String beneficiary, String accountId, String rate, Money amount) {}
    }
}

