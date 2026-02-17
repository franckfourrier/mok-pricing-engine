package com.kratos.mok.pricing.app.infrastructure.external;

import com.kratos.mok.pricing.app.api.ExternalAccountCreditor;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class HttpExternalAccountCreditor implements ExternalAccountCreditor {

    private final RestClient restClient;

    public HttpExternalAccountCreditor(RestClient.Builder builder, ExternalAccountsProperties props) {
        this.restClient = builder.baseUrl(props.baseUrl()).build();
    }

    @Override
    public void credit(String beneficiary, String accountId, Money amount, String idempotencyKey, String description) {

        if (amount == null || amount.isZero()) return;

        var req = new CreditRequest(
                accountId,
                amount.amount().toPlainString(),
                amount.currency(),
                beneficiary,
                description,
                idempotencyKey
        );

        try {
            restClient.post()
                    .uri("/v1/accounts/credit")
                    .header("Idempotency-Key", idempotencyKey)
                    .body(req)
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                log.info("External credit already processed (idempotent): key={}", idempotencyKey);
                return;
            }
            throw e;
        }
    }

    public record CreditRequest(
            String accountId,
            String amount,
            String currency,
            String beneficiary,
            String description,
            String reference
    ) {}
}
