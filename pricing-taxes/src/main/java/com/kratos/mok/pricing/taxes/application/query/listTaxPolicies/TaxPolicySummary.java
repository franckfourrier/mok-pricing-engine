package com.kratos.mok.pricing.taxes.application.query.listTaxPolicies;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;

import java.time.LocalDateTime;

public record TaxPolicySummary(
         /*SString id,
        String name,
        TransactionType transactionType,
        TargetScope targetScope,
        String targetValue,
        String taxMode,
        String strategyType,
        String value,
        String status,
        LocalDateTime createdAt*/

       String id,

        // Affichage métier
        String appliedTransaction,   // "Retrait", "Transfert", etc.
        String type,                 // "Fixe" | "Variable"
        String value,                // "0.05 %" | "500 XAF"

        // Périmètre
        TransactionType transactionType,
        TargetScope targetScope,
        String targetValue,

        // Statut affichable
        String status,               // "Validé", "En attente", ...

        // Date formatée pour UI
        String createdAt
) {}
