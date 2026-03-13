package com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto.updateTaxPolicy;

import com.kratos.mok.pricing.taxes.application.command.updateTaxPolicy.UpdateTaxPolicyCommand;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;

public final class UpdateTaxPolicyCommandMapper {

    private UpdateTaxPolicyCommandMapper() {
    }

    public static UpdateTaxPolicyCommand toCommand(String policyId, UpdateTaxPolicyRequest r) {
        TaxMode mode = r.mode() != null
                ? r.mode()
                : TaxMode.CANTONNEMENT;

        return new UpdateTaxPolicyCommand(
                policyId,
                r.transactionCodes(),
                mode,
                r.strategyType(),
                r.rate(),
                r.fixedAmount(),
                r.fluxIntensity(),
                Boolean.TRUE.equals(r.exempted())
        );
    }
}
