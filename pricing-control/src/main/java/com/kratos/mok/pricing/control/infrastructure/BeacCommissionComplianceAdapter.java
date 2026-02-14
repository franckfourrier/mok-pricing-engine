package com.kratos.mok.pricing.control.infrastructure;

import com.kratos.mok.pricing.commissions.domain.compliance.CommissionPlanComplianceData;
import com.kratos.mok.pricing.commissions.domain.enums.CommissionStrategyType;
import com.kratos.mok.pricing.commissions.domain.gateway.CommissionRegulatoryGatekeeper;
import com.kratos.mok.pricing.shared.domain.exception.RegulatoryViolationException;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@Profile("control")
public class BeacCommissionComplianceAdapter implements CommissionRegulatoryGatekeeper {

    @Override
    public void validate(CommissionPlanComplianceData data) throws RegulatoryViolationException {
        if (data == null) return;

        if (data.transactionType() == null) {
            throw new RegulatoryViolationException("BEAC_COMMISSION_TX_TYPE_REQUIRED", "transactionType is required");
        }
        if (data.target() == null) {
            throw new RegulatoryViolationException("BEAC_COMMISSION_TARGET_REQUIRED", "target is required");
        }
        if (data.strategyType() == null) {
            throw new RegulatoryViolationException("BEAC_COMMISSION_STRATEGY_REQUIRED", "strategyType is required");
        }

        if (data.transactionType() == TransactionType.DEPOSIT) {
            /*if (data.strategyType() == CommissionStrategyType.DEPOSIT_DISTRIBUTION) {
                throw new RegulatoryViolationException("BEAC_DEPOSIT_STRATEGY_FORBIDDEN",
                        "DEPOSIT_DEFERRED is forbidden for DEPOSIT");
            }*/
        }

         if (data.transactionType() == TransactionType.WITHDRAWAL) {
            /*var allowed = EnumSet.of(
                    CommissionStrategyType.WITHDRAWAL_AGENT_KRATOS,
                    CommissionStrategyType.DIRECT
            );
            if (!allowed.contains(data.strategyType())) {
                throw new RegulatoryViolationException("BEAC_WITHDRAWAL_STRATEGY_NOT_ALLOWED",
                        "Strategy not allowed for WITHDRAWAL: " + data.strategyType());
            }*/
        }
    }
}
