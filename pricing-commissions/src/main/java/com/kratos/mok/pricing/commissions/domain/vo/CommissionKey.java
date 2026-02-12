package com.kratos.mok.pricing.commissions.domain.vo;

import com.kratos.mok.pricing.commissions.domain.enums.BeneficiaryType;

import java.util.Objects;

public record CommissionKey(BeneficiaryType beneficiaryType, Percentage share) {
    public CommissionKey {
        Objects.requireNonNull(beneficiaryType, "beneficiaryType is required");
        Objects.requireNonNull(share, "share is required");
    }
}
