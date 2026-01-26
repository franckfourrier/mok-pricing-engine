package com.kratos.mok.pricing.fees.domain.gateway;

import com.kratos.mok.pricing.fees.domain.compliance.FeePolicyComplianceData;
import com.kratos.mok.pricing.shared.domain.exception.RegulatoryViolationException;

public interface RegulatoryGatekeeper {
    void validate(FeePolicyComplianceData data) throws RegulatoryViolationException;
}

