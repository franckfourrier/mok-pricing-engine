package com.kratos.mok.pricing.app.application.reference;

import com.kratos.mok.pricing.fees.infrastructure.repository.JpaFeePolicyRepository;
import com.kratos.mok.pricing.shared.api.reference.FeePolicyOptionDto;
import com.kratos.mok.pricing.shared.api.reference.TransactionCodeDto;
import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FeePolicyReferenceService {

    private final JpaFeePolicyRepository feePolicyRepository;

    public java.util.List<TransactionCodeDto> availableTransactionCodesForFees() {
        Set<String> configuredCodes = feePolicyRepository.findConfiguredTransactionCodes().stream()
                .map(v -> v.getTransactionCode())
                .collect(java.util.stream.Collectors.toSet());

        return Arrays.stream(TransactionCode.values())
                .filter(TransactionCode::supportsFees)
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

    public java.util.List<FeePolicyOptionDto> availableFeePolicyOptions() {

        Set<String> configuredKeys = feePolicyRepository.findConfiguredOptions().stream()
                .map(v -> key(
                        v.getTransactionCode(),
                        v.getTargetScope(),
                        v.getTargetValue()
                ))
                .collect(java.util.stream.Collectors.toSet());

        return buildAllOptions()
                .filter(option -> !configuredKeys.contains(
                        key(option.transactionCode(), option.targetScope(), option.targetValue())
                ))
                .toList();
    }

    private Stream<FeePolicyOptionDto> buildAllOptions() {
        return Arrays.stream(TransactionCode.values())
                .filter(TransactionCode::supportsFees)
                .flatMap(tx -> Arrays.stream(AccountType.values())
                        .map(accountType -> new FeePolicyOptionDto(
                                tx.name(),
                                tx.label(),
                                tx.transactionType().name(),
                                TargetScope.ACCOUNT_TYPE.name(),
                                accountType.name(),
                                accountType.label()
                        )));
    }

    private String key(String transactionCode, String targetScope, String targetValue) {
        return transactionCode + "|" + targetScope + "|" + targetValue;
    }
}
