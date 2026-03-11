package com.kratos.mok.pricing.app.application.reference;

import com.kratos.mok.pricing.commissions.infrastructure.repository.JpaCommissionPlanRepository;
import com.kratos.mok.pricing.shared.api.reference.TransactionCodeDto;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommissionPlanReferenceService {

    private final JpaCommissionPlanRepository commissionPlanRepository;

    public java.util.List<TransactionCodeDto> availableTransactionCodesForCommissions() {
        Set<String> configuredCodes = commissionPlanRepository.findConfiguredTransactionCodes().stream()
                .map(v -> v.getTransactionCode())
                .collect(java.util.stream.Collectors.toSet());

        return Arrays.stream(TransactionCode.values())
                .filter(TransactionCode::supportsCommissions)
                .filter(tc -> !configuredCodes.contains(tc.name()))
                .map(tc -> new TransactionCodeDto(
                        tc.name(),
                        tc.label(),
                        tc.transactionType().name()
                ))
                .toList();
    }
}