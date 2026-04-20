package com.kratos.mok.pricing.commissions.application.query.listCommissionPlans;

import com.kratos.mok.pricing.commissions.domain.enums.BeneficiaryType;
import com.kratos.mok.pricing.commissions.domain.enums.CommissionPlanStatus;
import com.kratos.mok.pricing.commissions.domain.strategy.*;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;
import com.kratos.mok.pricing.commissions.infrastructure.model.CommissionPlanEntity;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;

import java.util.Comparator;
import java.util.List;

import static com.kratos.mok.pricing.shared.domain.enums.TransactionCode.SUBSCRIBER_DEPOSIT;
import static com.kratos.mok.pricing.shared.domain.enums.TransactionCode.SUBSCRIBER_WITHDRAWAL;

public final class CommissionPlanReadMapper {

    private CommissionPlanReadMapper() {}

    public static CommissionPlanSummary toSummary(CommissionPlanEntity e) {

        List<CommissionLineSummary> lines = toLines(e.getTransactionCode(), e.getStrategy());

        CommissionPlanStatus st = e.getStatus();
        String statusLabel = switch (st) {
            case ACTIVE -> "Validé";
            case PENDING_APPROVAL, DRAFT -> "En attente";
            case BLOCKED -> "Bloqué";
            case SUSPENDED -> "Suspendu";
            default -> st.name();
        };

        // Génération du Short ID (8 caractères)
        String shortId = e.getId().length() > 8
                ? e.getId().substring(0, 8).toUpperCase()
                : e.getId();

        return new CommissionPlanSummary(
                e.getId(),
                shortId,
                e.getTransactionCode(),
                toTxLabel(e.getTransactionCode()),
                lines,
                e.getCreatedBy() == null ? null : e.getCreatedBy().getTimestamp(),
                statusLabel,
                st.name()
        );
    }

    private static List<CommissionLineSummary> toLines(TransactionCode txCode, CommissionStrategy strategy) {
        if (strategy == null) return List.of();

        String baseLabel = baseLabel(txCode, strategy); // "des frais de retrait (potentiel)" etc.

        // DepositDistributionStrategy: liste de CommissionShare (beneficiary + percentage)
        if (strategy instanceof SubscriberDepositStrategy s) {
            return s.keys().stream()
                    .sorted(Comparator.comparingInt(x -> beneficiaryOrder(x.beneficiaryType())))
                    .map(x -> line(x, baseLabel))
                    .toList();
        }

        // DirectStrategy: liste de CommissionShare
        if (strategy instanceof DirectStrategy s) {
            return s.keys().stream()
                    .sorted(Comparator.comparingInt(x -> beneficiaryOrder(x.beneficiaryType())))
                    .map(x -> line(x, baseLabel))
                    .toList();
        }

        // WithdrawalAgentKratosStrategy: agentShare + kratosShare (+ coverageRate)
        if (strategy instanceof SubscriberWithdrawalStrategy s) {
            //var agent = new CommissionShare(BeneficiaryType.AGENT, s.agentShare());
            //var kratos = new CommissionShare(BeneficiaryType.KRATOS, s.kratosShare());
            //return List.of(line(agent, baseLabel), line(kratos, baseLabel));
            return s.keys().stream()
                    .sorted(Comparator.comparingInt(x -> beneficiaryOrder(x.beneficiaryType())))
                    .map(x -> line(x, baseLabel))
                    .toList();
        }

        // fallback
        return List.of(new CommissionLineSummary("UNKNOWN", "Inconnu", "Stratégie: " + strategy.getClass().getSimpleName()));
    }

    private static CommissionLineSummary line(CommissionShare share, String baseLabel) {
        BeneficiaryType b = share.beneficiaryType();
        String pct = toPercentHuman(share.share().value()); // ex "0.10" -> "10%"
        return new CommissionLineSummary(
                b.name(),
                beneficiaryLabel(b),
                pct + " " + baseLabel
        );
    }

    private static String toPercentHuman(java.math.BigDecimal v) {
        // v attendu entre 0 et 1 : 0.10 => 10%
        var pct = v.multiply(java.math.BigDecimal.valueOf(100));
        // évite "10.0%"
        var stripped = pct.stripTrailingZeros();
        return stripped.toPlainString() + "%";
    }

    private static int beneficiaryOrder(BeneficiaryType b) {
        return switch (b) {
            case SUPER_DISTRIBUTOR -> 1;
            case DISTRIBUTOR -> 2;
            case AGENT -> 3;
            case KRATOS -> 4;
            default -> 99;
        };
    }

    private static String beneficiaryLabel(BeneficiaryType b) {
        return switch (b) {
            case SUPER_DISTRIBUTOR -> "Super Distributeur";
            case DISTRIBUTOR -> "Distributeur";
            case AGENT -> "Agent";
            case KRATOS -> "Kratos";
            default -> b.name();
        };
    }

    private static String toTxLabel(TransactionCode tc) {
        return switch (tc) {
            case SUBSCRIBER_DEPOSIT -> SUBSCRIBER_DEPOSIT.label();
            case SUBSCRIBER_WITHDRAWAL -> SUBSCRIBER_WITHDRAWAL.label();
            default -> tc.label();
        };
    }

    private static String baseLabel(TransactionCode txCode, CommissionStrategy strategy) {
        if (txCode == SUBSCRIBER_WITHDRAWAL) {
            return "des frais de retrait";
        }
        if (txCode == SUBSCRIBER_DEPOSIT && strategy instanceof SubscriberDepositStrategy) {
            return "des frais de retrait (potentiel)";
        }
        return "des frais de transaction";
    }
}