package com.kratos.mok.pricing.fees.domain.repository;

public interface FeePolicyConfiguredOptionView {
    String getTransactionCode();
    String getTargetScope();
    String getTargetValue();
}