package com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto.createTaxPolicy;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.taxes.application.command.createTaxPolicy.CreateTaxPolicyCommand;
import com.kratos.mok.pricing.taxes.domain.enums.TaxMode;

public class CreateTaxPolicyCommandMapper {

    public static CreateTaxPolicyCommand toCommand(CreateTaxPolicyRequest r) {

        TaxMode mode = r.mode() != null
                ? r.mode()
                : TaxMode.CANTONNEMENT;

        return new CreateTaxPolicyCommand(
                r.transactionCodes(),
                TargetScope.GLOBAL,
                "ALL",
                r.currency(),
                mode,
                r.strategyType(),
                r.rate(),
                r.fixedAmount(),
                r.fluxIntensity(),
                Boolean.TRUE.equals(r.exempted()) // défaut false si null
        );
    }
}
