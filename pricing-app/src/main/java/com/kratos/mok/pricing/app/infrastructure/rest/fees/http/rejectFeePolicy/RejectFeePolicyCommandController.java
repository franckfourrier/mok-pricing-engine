package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.rejectFeePolicy;

import com.kratos.mok.pricing.app.infrastructure.config.swagger.OpenApiConfig;
import com.kratos.mok.pricing.fees.application.command.rejectFeePolicy.RejectFeePolicyCommand;
import com.kratos.mok.pricing.fees.application.command.rejectFeePolicy.RejectFeePolicyCommandHandler;
import com.kratos.mok.pricing.fees.application.command.rejectFeePolicy.RejectFeePolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/fee-policies")
@RequiredArgsConstructor
public class RejectFeePolicyCommandController {

    private final RejectFeePolicyCommandHandler handler;

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @Operation(
            summary = "Reject a fee policy",
            description = "Requires role: SUPER_ADMIN",
            security = { @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH) }
    )
    @ApiResponse(responseCode = "200", description = "Rejected")
    @ApiResponse(responseCode = "403", description = "Forbidden (missing role SUPER_ADMIN)")
    public ResponseEntity<RejectFeePolicyResponse> approve(
            @PathVariable String id,
            @Valid @RequestBody(required = false) RejectFeePolicyRequest body,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        String reason = (body == null) ? null : body.reason();
        var cmd = new RejectFeePolicyCommand(id, reason);

        return ResponseEntity.ok(handler.handle(cmd, actor));
    }

    public record RejectFeePolicyRequest(String reason) {}

    private String resolveAuthorId(Jwt jwt) {
        if (jwt == null) return "UNKNOWN";

        String sub = jwt.getSubject();
        if (sub != null && !sub.isBlank()) return sub;

        String username = jwt.getClaimAsString("preferred_username");
        if (username != null && !username.isBlank()) return username;

        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) return email;

        return "UNKNOWN";
    }
}
