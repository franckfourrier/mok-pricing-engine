package com.kratos.mok.pricing.app.api;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public interface ExternalAccountCreditor {
    void credit(String beneficiary,
                String accountId,
                Money amount,
                String idempotencyKey,
                String description);
}

