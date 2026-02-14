package com.kratos.mok.pricing.app.application.port;

import com.kratos.mok.pricing.shared.domain.vo.Money;

public interface ExternalAccountCreditor {

    CreditResult credit(String externalAccountId, Money amount, String externalTxId, String beneficiary);

    record CreditResult(boolean success, String status, String reference) {}
}
