package com.kratos.mok.pricing.fees.domain.gateway;

import com.kratos.mok.pricing.fees.domain.compliance.FeePolicyComplianceData;

public interface RegulatoryGatekeeper {
    /**
     * @throws RegulatoryViolationException si non conforme (Arrêt Réglementaire)
     */
    void validate(FeePolicyComplianceData data);
}
