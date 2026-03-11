package com.kratos.mok.pricing.app.infrastructure.rest.reference;

import com.kratos.mok.pricing.app.application.reference.FeePolicyReferenceService;
import com.kratos.mok.pricing.shared.api.reference.AccountTypeDto;
import com.kratos.mok.pricing.shared.api.reference.FeePolicyOptionDto;
import com.kratos.mok.pricing.shared.api.reference.TransactionCodeDto;
import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/v1/reference")
@RequiredArgsConstructor
@Tag(name = "Reference", description = "Reference data for frontend configuration")
public class ReferenceController {

    private final FeePolicyReferenceService feePolicyReferenceService;

    @Operation(summary = "List all transaction codes")
    @GetMapping("/transaction-codes")
    public List<TransactionCodeDto> transactionCodes() {
        return Arrays.stream(TransactionCode.values())
                .map(this::toDto)
                .toList();
    }

    @Operation(summary = "List available transaction codes for fees")
    @GetMapping("/transaction-codes/fees")
    public List<TransactionCodeDto> transactionCodesForFees() {
        return feePolicyReferenceService.availableTransactionCodesForFees();
    }

    @Operation(summary = "Transaction codes supporting TAXES")
    @GetMapping("/transaction-codes/taxes")
    public List<TransactionCodeDto> transactionCodesForTaxes() {
        return Arrays.stream(TransactionCode.values())
                .filter(TransactionCode::supportsTaxes)
                .map(this::toDto)
                .toList();
    }

    @Operation(summary = "Transaction codes supporting COMMISSIONS")
    @GetMapping("/transaction-codes/commissions")
    public List<TransactionCodeDto> transactionCodesForCommissions() {
        return Arrays.stream(TransactionCode.values())
                .filter(TransactionCode::supportsCommissions)
                .map(this::toDto)
                .toList();
    }

    @Operation(summary = "List account types")
    @GetMapping("/account-types")
    public List<AccountTypeDto> accountTypes() {
        return Arrays.stream(AccountType.values())
                .map(v -> new AccountTypeDto(
                        v.name(),
                        v.label()
                ))
                .toList();
    }

    @Operation(summary = "List available fee policy options")
    @GetMapping("/fee-policy-options")
    public List<FeePolicyOptionDto> feePolicyOptions() {
        return feePolicyReferenceService.availableFeePolicyOptions();
    }

    private TransactionCodeDto toDto(TransactionCode v) {
        return new TransactionCodeDto(
                v.name(),
                v.label(),
                v.transactionType().name()
        );
    }
}