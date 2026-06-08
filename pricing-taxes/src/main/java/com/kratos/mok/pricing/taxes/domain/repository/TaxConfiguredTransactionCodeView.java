package com.kratos.mok.pricing.taxes.domain.repository;

import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;

public interface TaxConfiguredTransactionCodeView {
    String getTransactionCode();
    TaxStrategyType getStrategyType();
}