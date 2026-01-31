package com.kratos.mok.pricing.control.infrastructure.beac;

import com.kratos.mok.pricing.shared.domain.TransactionComplianceData;
import com.kratos.mok.pricing.shared.domain.exception.RegulatoryViolationException;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class BeacTransactionComplianceAdapter {

    private final BeacThresholdsProperties props;

    public BeacTransactionComplianceAdapter(BeacThresholdsProperties props) {
        this.props = props;
    }

    public void validate(TransactionComplianceData data) {
        String ccy = props.normalizedCurrency();

        // 1) currency check (si ton système impose XAF)
        if (!data.amount().currency().equals(ccy)) {
            throw new RegulatoryViolationException("BEAC_CURRENCY_NOT_ALLOWED",
                    "Devise non autorisée: " + data.amount().currency());
        }

        // 2) Per-operation limit (si défini pour ce type)
        Money opLimit = props.valueLimits().perOperationMoney(data.type(), ccy);
        if (opLimit != null && data.amount().compareTo(opLimit) > 0) {
            throw new RegulatoryViolationException("BEAC_PER_OPERATION_LIMIT_EXCEEDED",
                    "Plafond par opération dépassé pour " + data.type() + " (limit=" + opLimit + ")");
        }

        // 3) Monthly volume limit (si on a l’info)
        Integer maxMonthly = props.volumeMonthly().get(data.type());
        if (maxMonthly != null && data.monthlyCount() != null && data.monthlyCount() > maxMonthly) {
            throw new RegulatoryViolationException("BEAC_MONTHLY_VOLUME_LIMIT_EXCEEDED",
                    "Volume mensuel dépassé pour " + data.type() + " (limit=" + maxMonthly + ")");
        }

        // 4) Daily limit (si défini + si tu as une agrégation daily amount côté système)
        // Ici tu peux rajouter plus tard daily totals / combined totals.
    }
}

