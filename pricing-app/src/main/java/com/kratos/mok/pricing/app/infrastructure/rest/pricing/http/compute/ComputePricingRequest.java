package com.kratos.mok.pricing.app.infrastructure.rest.pricing.http.compute;

import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.OffsetDateTime;

import static com.kratos.mok.pricing.shared.domain.enums.TransactionCode.SUBSCRIBER_DEPOSIT;
import static com.kratos.mok.pricing.shared.domain.enums.TransactionCode.SUBSCRIBER_WITHDRAWAL;

public record ComputePricingRequest(
        @Schema(description = "Code de transaction", example = "SUBSCRIBER_DEPOSIT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le code de transaction est obligatoire")
        String transactionCode,

        @Schema(description = "Inclure frais de retrait (dépôt pour retrait)", defaultValue = "false")
        Boolean includeWithdrawalFees,

        @Schema(description = "Montant de la transaction", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le montant est obligatoire")
        @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "Le montant doit être un nombre valide")
        String amount,

        @Schema(description = "Devise (par défaut XAF)", example = "XAF")
        String currency,

        @Schema(description = "ID du compte client", example = "ACC-123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "L'ID du compte est obligatoire")
        String accountId,

        @Schema(description = "Type de compte", example = "SAVINGS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le type de compte est obligatoire")
        String accountType,

        @Schema(description = "Statut KYC du client")
        boolean kycValidated,

        @Schema(description = "Nombre de transactions ce mois-ci")
        Integer monthlyTxCount,

        @Schema(description = "Date de l'opération (ISO 8601)")
        OffsetDateTime occurredAt
) {
    public PricingRequestContext toDomainContext(OffsetDateTime defaultNow) {

        TransactionCode txCode = parseTransactionCode(transactionCode);
        AccountType accType = parseAccountType(accountType);

        // 1. On calcule d'abord la valeur booléenne sécurisée (Null-safe)
        boolean effectiveInclude = (includeWithdrawalFees == null) ? false : includeWithdrawalFees;

        // 2. On utilise cette valeur propre pour le code effectif
        TransactionCode effectiveCode = (txCode == SUBSCRIBER_DEPOSIT && effectiveInclude)
                ? SUBSCRIBER_WITHDRAWAL
                : txCode;

        // 3. Le reste reste inchangé
        String effectiveCurrency = (currency == null || currency.isBlank()) ? "XAF" : currency.trim().toUpperCase();
        OffsetDateTime effectiveDate = (occurredAt == null) ? defaultNow : occurredAt;

        return new PricingRequestContext(
                effectiveCode,
                Money.of(amount, effectiveCurrency),
                accountId,
                accType,
                kycValidated,
                monthlyTxCount == null ? 0 : monthlyTxCount,
                effectiveDate
        );
    }

    private TransactionCode parseTransactionCode(String code) {
        try {
            return TransactionCode.valueOf(code.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Code de transaction invalide : " + code);
        }
    }

    private AccountType parseAccountType(String type) {
        try {
            return AccountType.valueOf(type.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Type de compte invalide : " + type);
        }
    }
}