package com.kratos.mok.pricing.app.infrastructure.rest.pricing.http.bankDeposit;

import com.kratos.mok.pricing.app.application.command.bankDeposit.RecordBankDepositCommand;
import com.kratos.mok.pricing.app.application.command.bankDeposit.RecordBankDepositCommandHandler;
import com.kratos.mok.pricing.app.application.command.bankDeposit.RecordBankDepositResponse;
import com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.bankDeposit.BankDepositNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pricing")
@RequiredArgsConstructor
public class RecordBankDepositCommandController {

    private final RecordBankDepositCommandHandler handler;

    @PostMapping("/bank-deposits")
    @PreAuthorize("hasAnyRole('SYSTEM','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<RecordBankDepositResponse> record(
            @RequestBody BankDepositNotificationRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank()) ? actorId.trim() : resolveAuthorId(jwt);

        var cmd = new RecordBankDepositCommand(
                request.referenceVersement(),
                request.montant(),
                request.superDistributeur()
        );

        return ResponseEntity.ok(handler.handle(cmd, actor));
    }

    private String resolveAuthorId(Jwt jwt) {
        if (jwt == null) return "UNKNOWN";
        String sub = jwt.getSubject();
        return (sub != null && !sub.isBlank()) ? sub : "UNKNOWN";
    }
}
