package com.kratos.mok.pricing.commissions.domain.gateway;

import com.kratos.mok.pricing.commissions.domain.compliance.CommissionPlanComplianceData;
import com.kratos.mok.pricing.shared.domain.exception.RegulatoryViolationException;

public interface CommissionRegulatoryGatekeeper {
    void validate(CommissionPlanComplianceData data) throws RegulatoryViolationException;
}
