package com.kratos.mok.pricing.commissions.application.query.listCommissionPlans;

import com.kratos.mok.pricing.commissions.infrastructure.model.CommissionPlanEntity;

import java.util.Comparator;
import java.util.List;

public final class CommissionPlanReadMapper {

    private CommissionPlanReadMapper() {}

    public static CommissionPlanSummary toSummary(CommissionPlanEntity e) {

        // ✅ À ADAPTER: supposons que e.getLines() renvoie une liste d'objets
        // contenant beneficiary + rule (percentage/fixed/base...)
        List<CommissionLineSummary> lines = e.getLines().stream()
                .sorted(Comparator.comparingInt(l -> beneficiaryOrder(l.getBeneficiary())))
                .map(l -> new CommissionLineSummary(
                        l.getBeneficiary().name(),
                        toLabel(l.getBeneficiary().name()),
                        toValueLabel(l) // "10% des frais de retrait (potentiel)" etc
                ))
                .toList();

        String statusRaw = String.valueOf(e.getStatus()); // ex: APPROVED / PENDING_APPROVAL
        String statusLabel = switch (statusRaw) {
            case "APPROVED", "ACTIVE" -> "Validé";
            case "PENDING_APPROVAL" -> "En attente";
            default -> statusRaw;
        };

        return new CommissionPlanSummary(
                e.getId(),
                e.getTransactionType(),
                lines,
                statusLabel,
                statusRaw,
                e.getCreatedBy().getTimestamp() // ou e.getCreatedAt() si tu l’as
        );
    }

    private static int beneficiaryOrder(Object beneficiaryEnumOrString) {
        String b = String.valueOf(beneficiaryEnumOrString);
        return switch (b) {
            case "SUPER_DISTRIBUTOR" -> 1;
            case "DISTRIBUTOR" -> 2;
            case "AGENT" -> 3;
            default -> 99;
        };
    }

    private static String toLabel(String code) {
        return switch (code) {
            case "SUPER_DISTRIBUTOR" -> "Super Distributeur";
            case "DISTRIBUTOR" -> "Distributeur";
            case "AGENT" -> "Agent";
            default -> code;
        };
    }

    private static String toValueLabel(Object lineEntity) {
        // ✅ À ADAPTER: ton lineEntity contient sûrement:
        // - base (WITHDRAWAL_FEE_REAL / WITHDRAWAL_FEE_ESTIMATED)
        // - percentage or fixed
        // Exemple de rendu conforme UI:

        // PSEUDO:
        // var l = (CommissionLineEntity) lineEntity;
        // if (l.getPercentage() != null) return l.getPercentage() + "% des frais de retrait (potentiel)";
        // return l.getFixedAmount() + " XAF";

        return String.valueOf(lineEntity); // remplace par ton format réel
    }
}
