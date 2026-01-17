package com.kratos.mok.pricing.control.infrastructure;

import com.kratos.mok.pricing.fees.domain.compliance.FeePolicyComplianceData;
import com.kratos.mok.pricing.fees.domain.gateway.RegulatoryGatekeeper;
import com.kratos.mok.pricing.fees.domain.gateway.RegulatoryViolationException;
import org.springframework.stereotype.Component;


@Component
public class BeacComplianceAdapter implements RegulatoryGatekeeper {

    @Override
    public void validate(FeePolicyComplianceData data) {
        // Exemple : plafond maxFee BEAC
        if (data.maxAmount() != null /* && data.maxFee() > plafond */) {
            throw new RegulatoryViolationException("BEAC_MAX_FEE_EXCEEDED",
                    "Plafond BEAC dépassé pour maxFee");
        }
    }
}
