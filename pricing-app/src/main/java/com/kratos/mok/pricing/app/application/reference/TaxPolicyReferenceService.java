package com.kratos.mok.pricing.app.application.reference;

import com.kratos.mok.pricing.shared.api.reference.TransactionCodeDto;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.taxes.infrastructure.repository.JpaTaxPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaxPolicyReferenceService {

    private final JpaTaxPolicyRepository taxPolicyRepository;

    public java.util.List<TransactionCodeDto> availableTransactionCodesForTaxes() {
        Set<String> configuredCodes = taxPolicyRepository.findConfiguredTransactionCodes().stream()
                .map(v -> v.getTransactionCode())
                .collect(java.util.stream.Collectors.toSet());

        return Arrays.stream(TransactionCode.values())
                .filter(TransactionCode::supportsTaxes)
                .filter(tc -> !configuredCodes.contains(tc.name()))
                .map(tc -> new TransactionCodeDto(
                        tc.name(),
                        tc.label(),
                        tc.transactionType().name()
                ))
                .toList();
    }
}