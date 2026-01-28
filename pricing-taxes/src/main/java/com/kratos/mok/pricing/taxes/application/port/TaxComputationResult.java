package com.kratos.mok.pricing.taxes.application.port;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public record TaxComputationResult(String taxPolicyId, Money tax) {}
