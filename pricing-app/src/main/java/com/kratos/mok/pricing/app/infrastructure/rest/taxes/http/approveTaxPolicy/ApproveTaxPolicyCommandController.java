package com.kratos.mok.pricing.app.infrastructure.rest.taxes.http.approveTaxPolicy;

import com.kratos.mok.pricing.taxes.application.command.approveTaxPolicy.ApproveTaxPolicyCommand;
import com.kratos.mok.pricing.taxes.application.command.approveTaxPolicy.ApproveTaxPolicyCommandHandler;
import com.kratos.mok.pricing.taxes.application.command.approveTaxPolicy.ApproveTaxPolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tax-policies")
@RequiredArgsConstructor
public class ApproveTaxPolicyCommandController {

    private final ApproveTaxPolicyCommandHandler handler;

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApproveTaxPolicyResponse> approve(
            @PathVariable String id,
            @Valid @RequestBody ApproveTaxPolicyRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        String reason = (request == null) ? null : request.reason();
        var cmd = new ApproveTaxPolicyCommand(id, reason);
        return ResponseEntity.ok(handler.handle(cmd, actor));

    }

    public record ApproveTaxPolicyRequest(String reason) {}

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
