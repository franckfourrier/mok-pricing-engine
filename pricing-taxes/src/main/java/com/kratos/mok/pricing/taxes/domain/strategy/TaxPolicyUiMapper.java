package com.kratos.mok.pricing.taxes.domain.strategy;

import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.taxes.infrastructure.model.TaxPolicyEntity;

import java.time.format.DateTimeFormatter;

public final class TaxPolicyUiMapper {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private TaxPolicyUiMapper() {}

    public static String appliedTransaction(TransactionType type) {
        if (type == null) return "Tous";

        return switch (type) {
            case WITHDRAWAL -> "Retrait";
            case DEPOSIT -> "Dépôt";
            case P2P_TRANSFER -> "Transfert";
            //case PAYMENT -> "Paiement";
            default -> type.name();
        };
    }

    public static String type(TaxPolicyEntity e) {
        return switch (e.getStrategyType()) {
            case FIXED_AMOUNT -> "Fixe";
            case ELECTRONIC_RATE -> "Variable";
            case NONE -> "Aucune";
        };
    }

    public static String name(TaxPolicyEntity e) {
        return switch (e.getStrategyType()) {
            case FIXED_AMOUNT -> "Forfaitaire";
            case ELECTRONIC_RATE -> "Electronique";
            case NONE -> "Exonéré";
        };
    }

    /*public static String value(TaxPolicyEntity e) {
        return switch (e.getStrategyType()) {
            case FIXED_AMOUNT ->
                    e.getFixedAmount() + " " + e.getCurrency();

            case ELECTRONIC_RATE ->
                    e.getRate().multiply(java.math.BigDecimal.valueOf(100))
                            .stripTrailingZeros()
                            .toPlainString() + " %";
        };
    }*/
    public static String value(TaxPolicyEntity e) {
        if (e.getStrategyType() == null) return "-";

        return switch (e.getStrategyType()) {
            case FIXED_AMOUNT ->
                    (e.getFixedAmount() != null ? e.getFixedAmount() : "0") + " " + (e.getCurrency() != null ? e.getCurrency() : "");

            case ELECTRONIC_RATE ->
                    (e.getRate() != null ?
                            e.getRate().stripTrailingZeros()
                                       .toPlainString() : "0") + " %";

            case NONE -> "0";
        };
    }

    /*public static String createdAt(TaxPolicyEntity e) {
        if (e.getCreatedAt() == null) return "";
        return e.getCreatedAt().format(DATE_FORMAT);
    }*/

    // --- MISE À JOUR POUR AUDITEMBEDDABLE ---
    public static String createdAt(TaxPolicyEntity e) {
        if (e.getCreatedBy() == null || e.getCreatedBy().getTimestamp() == null) {
            return "";
        }
        return e.getCreatedBy().getTimestamp().format(DATE_FORMAT);
    }

    public static String author(TaxPolicyEntity e) {
        if (e.getCreatedBy() == null || e.getCreatedBy().getAuthor() == null) {
            return "Système";
        }
        return e.getCreatedBy().getAuthor();
    }
}
