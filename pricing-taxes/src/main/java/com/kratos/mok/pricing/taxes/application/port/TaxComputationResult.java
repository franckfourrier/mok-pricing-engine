package com.kratos.mok.pricing.taxes.application.port;

import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.util.List;

public record TaxComputationResult(
        Money totalTax,
        List<TaxLine> lines
) {}
