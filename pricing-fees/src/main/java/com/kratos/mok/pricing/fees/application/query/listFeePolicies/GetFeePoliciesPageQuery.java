package com.kratos.mok.pricing.fees.application.query.listFeePolicies;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;

public record GetFeePoliciesPageQuery(
        int page,
        int size,
        TransactionType transactionType,
        TargetScope targetScope,
        String targetValue,
        String status
) {}
