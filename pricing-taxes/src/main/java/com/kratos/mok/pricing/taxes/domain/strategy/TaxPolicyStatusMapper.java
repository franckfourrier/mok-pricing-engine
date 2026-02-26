package com.kratos.mok.pricing.taxes.domain.strategy;

import com.kratos.mok.pricing.taxes.domain.enums.TaxPolicyStatus;

public final class TaxPolicyStatusMapper {

    private TaxPolicyStatusMapper() {}

    public static String toLabel(TaxPolicyStatus status) {
        if (status == null) return "UNKNOWN";

        return switch (status) {
            case ACTIVE -> "Validé";
            case PENDING_APPROVAL -> "En attente";
            case REJECTED -> "Rejetée";
            case SUSPENDED -> "Suspendue";
            case BLOCKED -> "Bloquée";
            case ARCHIVED -> "Archivée";
            case DRAFT -> "Brouillon";
        };
    }
}