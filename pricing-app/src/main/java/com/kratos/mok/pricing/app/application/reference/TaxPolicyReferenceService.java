package com.kratos.mok.pricing.app.application.reference;

import com.kratos.mok.pricing.shared.api.reference.TransactionCodeDto;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.taxes.domain.enums.TaxStrategyType;
import com.kratos.mok.pricing.taxes.domain.repository.TaxConfiguredTransactionCodeView;
import com.kratos.mok.pricing.taxes.infrastructure.repository.JpaTaxPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxPolicyReferenceService {

    private final JpaTaxPolicyRepository taxPolicyRepository;

    public List<TransactionCodeDto> availableTransactionCodesForTaxes(
            @Nullable TaxStrategyType strategyType
    ) {
        // Récupère tous les codes configurés avec leur stratégie
        Set<String> configuredCodes = taxPolicyRepository.findConfiguredTransactionCodes()
                .stream()
                .filter(view -> shouldFilterByStrategy(view, strategyType))
                .map(TaxConfiguredTransactionCodeView::getTransactionCode)
                .collect(Collectors.toSet());

        return Arrays.stream(TransactionCode.values())
                .filter(TransactionCode::supportsTaxes)
                .filter(tc -> !configuredCodes.contains(tc.name()))
                .map(tc -> new TransactionCodeDto(
                        tc.name(),
                        tc.label(),
                        tc.transactionType().name(),
                        tc.sender(),
                        tc.receiver()
                ))
                .toList();
    }

    private boolean shouldFilterByStrategy(TaxConfiguredTransactionCodeView view, TaxStrategyType requested) {
        if (requested == null) {
            return true; // Si aucun filtre, on exclut tous les codes déjà configurés (peu importe la stratégie)
        }
        return view.getStrategyType() == requested; // On exclut uniquement ceux avec la stratégie demandée
    }
}