package com.kratos.mok.pricing.app.infrastructure.rest.fees.dto;

import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;

public record FeePolicySearchCriteria(
        TransactionCode transactionCode,
        String status
) {}
