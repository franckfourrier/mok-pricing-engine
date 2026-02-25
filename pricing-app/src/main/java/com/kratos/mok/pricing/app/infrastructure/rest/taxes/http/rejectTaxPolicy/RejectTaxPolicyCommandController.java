package com.kratos.mok.pricing.app.infrastructure.rest.taxes.http.rejectTaxPolicy;

import com.kratos.mok.pricing.taxes.application.command.approveTaxPolicy.ApproveTaxPolicyCommand;
import com.kratos.mok.pricing.taxes.application.command.approveTaxPolicy.ApproveTaxPolicyCommandHandler;
import com.kratos.mok.pricing.taxes.application.command.approveTaxPolicy.ApproveTaxPolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tax-policies")
@RequiredArgsConstructor
public class RejectTaxPolicyCommandController {

    private final ApproveTaxPolicyCommandHandler handler;

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApproveTaxPolicyResponse> approve(
            @PathVariable String id,
            @Valid @RequestBody RejectTaxPolicyRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        String reason = (request == null) ? null : request.reason();
        var cmd = new RejectTaxPolicyCommand(id, reason);
        return ResponseEntity.ok(handler.handle(cmd, actor));

    }

    public record RejectTaxPolicyRequest(String reason) {}

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
