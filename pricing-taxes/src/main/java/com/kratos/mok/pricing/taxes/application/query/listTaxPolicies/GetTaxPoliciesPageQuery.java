package com.kratos.mok.pricing.taxes.application.query.listTaxPolicies;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;

public record GetTaxPoliciesPageQuery(
        int page,
        int size,
        TransactionType transactionType,
        TargetScope targetScope,
        String targetValue,
        String status
) {}
