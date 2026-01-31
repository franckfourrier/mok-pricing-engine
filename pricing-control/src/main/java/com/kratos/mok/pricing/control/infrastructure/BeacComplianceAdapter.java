package com.kratos.mok.pricing.control.infrastructure;

import com.kratos.mok.pricing.control.infrastructure.beac.BeacThresholdsProperties;
import com.kratos.mok.pricing.fees.domain.compliance.FeePolicyComplianceData;
import com.kratos.mok.pricing.fees.domain.gateway.RegulatoryGatekeeper;
import com.kratos.mok.pricing.shared.domain.TransactionComplianceData;
import com.kratos.mok.pricing.shared.domain.exception.RegulatoryViolationException;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.springframework.stereotype.Component;


@Component
public class BeacComplianceAdapter implements RegulatoryGatekeeper {

    private final BeacThresholdsProperties props;

    public BeacComplianceAdapter(BeacThresholdsProperties props) {
        this.props = props;
    }

    @Override
    public void validate(FeePolicyComplianceData data)  throws RegulatoryViolationException{
        String ccy = props.normalizedCurrency();

        // Impose XAF)
        if (data.activationThreshold() != null && !data.activationThreshold().currency().equals(ccy)) {
            throw new RegulatoryViolationException("BEAC_CURRENCY_NOT_ALLOWED",
                    "Devise non autorisée: " + data.activationThreshold().currency());
        }

        // règle BEAC sur maxFee (à définir selon le métier)
        // exemple: maxFee ne doit pas dépasser le plafond par opération du type
        Money opLimit = props.valueLimits().perOperationMoney(data.transactionType(), ccy);

        if (data.maxAmount() != null && opLimit != null && data.minAmount().compareTo(opLimit) > 0) {
            throw new RegulatoryViolationException("BEAC_MAX_FEE_EXCEEDED",
                    "Plafond BEAC dépassé pour maxFee (maxFee=" + data.maxAmount() + ", limit=" + opLimit + ")");
        }

        // optionnel: minFee <= maxFee
        if (data.maxAmount() != null && data.maxAmount() != null && data.minAmount().compareTo(data.maxAmount()) > 0) {
            throw new RegulatoryViolationException("BEAC_INVALID_FEE_RANGE",
                    "minFee > maxFee");
        }
    }

    @Override
    public void validate(TransactionComplianceData data) throws RegulatoryViolationException {

    }
}
