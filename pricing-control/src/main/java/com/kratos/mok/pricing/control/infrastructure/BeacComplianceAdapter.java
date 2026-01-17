package com.kratos.mok.pricing.control.infrastructure;

import com.kratos.mok.pricing.fees.domain.compliance.FeePolicyComplianceData;
import com.kratos.mok.pricing.fees.domain.gateway.RegulatoryGatekeeper;
import org.springframework.stereotype.Service;

@Service
public class BeacComplianceAdapter implements RegulatoryGatekeeper {

    @Override
    public void validate(FeePolicyComplianceData data) {
        // Logique "Plafonds fixés par la BEAC"
        // Si KO -> throw new RegulatoryViolationException(...)
    }
}
