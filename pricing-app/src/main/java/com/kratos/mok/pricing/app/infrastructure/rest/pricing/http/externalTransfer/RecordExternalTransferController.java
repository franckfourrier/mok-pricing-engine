package com.kratos.mok.pricing.app.infrastructure.rest.pricing.http.externalTransfer;

import com.kratos.mok.pricing.app.application.command.bankDeposit.BankDepositResponse;
import com.kratos.mok.pricing.app.application.command.externalTransfer.ExternalTransferCommandHandler;
import com.kratos.mok.pricing.app.application.command.externalTransfer.ExternalTransferResponse;
import com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.bankDeposit.BankDepositNotificationMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.bankDeposit.BankDepositNotificationRequest;
import com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.externalTransfer.ExternalTransferNotificationMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.externalTransfer.ExternalTransferNotificationRequest;
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
public class RecordExternalTransferController {

    private final ExternalTransferCommandHandler handler;

    @PostMapping("/cantonment/debits")
    @PreAuthorize("hasAnyRole('SYSTEM','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ExternalTransferResponse> record(
            @Valid @RequestBody ExternalTransferNotificationRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank()) ? actorId.trim() : resolveAuthorId(jwt);

        var cmd = ExternalTransferNotificationMapper.toCommand(request);
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
