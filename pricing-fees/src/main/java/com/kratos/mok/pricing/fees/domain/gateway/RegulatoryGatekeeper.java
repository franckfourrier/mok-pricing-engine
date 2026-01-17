package com.kratos.mok.pricing.fees.domain.gateway;

import com.kratos.mok.pricing.fees.domain.compliance.FeePolicyComplianceData;

public interface RegulatoryGatekeeper {
    void validate(FeePolicyComplianceData data) throws RegulatoryViolationException;
}

