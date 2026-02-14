package com.kratos.mok.pricing.app.infrastructure.external;

import com.kratos.mok.pricing.app.application.port.ExternalAccountCreditor;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class ExternalAccountCreditorHttpAdapter implements ExternalAccountCreditor {

    private final WebClient externalCoreClient;

    @Override
    public CreditResult credit(String externalAccountId, Money amount, String externalTxId, String beneficiary) {

        var req = new CreditRequest(
                externalTxId,
                externalAccountId,
                amount.amount().toPlainString(),
                amount.currency(),
                beneficiary
        );

        var res = externalCoreClient.post()
                .uri("/accounts/credit")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(CreditResponse.class)
                .block();

        if (res == null) return new CreditResult(false, "NO_RESPONSE", null);
        return new CreditResult(res.success(), res.status(), res.reference());
    }

    record CreditRequest(String externalTxId, String accountId, String amount, String currency, String beneficiary) {}
    record CreditResponse(boolean success, String status, String reference) {}
}
