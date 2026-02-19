package com.kratos.mok.pricing.app.infrastructure.rest.pricing.http.bankDeposit;

import com.kratos.mok.pricing.app.application.command.bankDeposit.RecordBankDepositCommandHandler;
import com.kratos.mok.pricing.app.application.command.bankDeposit.RecordBankDepositResponse;
import com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.bankDeposit.BankDepositNotificationMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.bankDeposit.BankDepositNotificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pricing")
@RequiredArgsConstructor
public class RecordCantonmentCreditController {

    private final RecordBankDepositCommandHandler handler;

    @PostMapping("/cantonment/credits")
    @PreAuthorize("hasAnyRole('SYSTEM','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<RecordBankDepositResponse> record(
            @Valid @RequestBody BankDepositNotificationRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank()) ? actorId.trim() : resolveAuthorId(jwt);

        var cmd = BankDepositNotificationMapper.toCommand(request);
        return ResponseEntity.ok(handler.handle(cmd, actor));
    }

    private String resolveAuthorId(Jwt jwt) {
        if (jwt == null) return "UNKNOWN";

        String sub = jwt.getSubject();
        if (sub != null && !sub.isBlank()) return sub;

        String clientId = jwt.getClaimAsString("client_id");
        if (clientId != null && !clientId.isBlank()) return clientId;

        return "UNKNOWN";
    }
}
