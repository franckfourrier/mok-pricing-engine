package com.kratos.mok.pricing.app.infrastructure.rest.pricing.http.applyPricingToTransaction;

import com.kratos.mok.pricing.app.application.command.applyPricingToTransaction.ApplyPricingToTransactionCommandHandler;
import com.kratos.mok.pricing.app.application.command.applyPricingToTransaction.ApplyPricingToTransactionResponse;
import com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.applyPricingToTransaction.ApplyPricingToTransactionCommandMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.pricing.dto.applyPricingToTransaction.ApplyPricingToTransactionRequest;
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
public class ApplyPricingToTransactionCommandController {

    private final ApplyPricingToTransactionCommandHandler handler;

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('SYSTEM','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplyPricingToTransactionResponse> apply(
            @Valid @RequestBody ApplyPricingToTransactionRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        var cmd = ApplyPricingToTransactionCommandMapper.toCommand(request);
        var res = handler.handle(cmd, actor);

        return ResponseEntity.ok(res);
    }

    private String resolveAuthorId(Jwt jwt) {
        if (jwt == null) return "UNKNOWN";

        String sub = jwt.getSubject();
        if (sub != null && !sub.isBlank()) return sub;

        String clientId = jwt.getClaimAsString("client_id");
        if (clientId != null && !clientId.isBlank()) return clientId;

        String username = jwt.getClaimAsString("preferred_username");
        if (username != null && !username.isBlank()) return username;

        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) return email;

        return "UNKNOWN";
    }
}
