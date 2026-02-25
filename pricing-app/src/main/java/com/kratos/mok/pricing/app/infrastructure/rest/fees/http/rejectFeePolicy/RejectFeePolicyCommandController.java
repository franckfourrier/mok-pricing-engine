package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.rejectFeePolicy;

import com.kratos.mok.pricing.fees.application.command.rejecteFeePolicy.RejectFeePolicyCommand;
import com.kratos.mok.pricing.fees.application.command.rejecteFeePolicy.RejectFeePolicyCommandHandler;
import com.kratos.mok.pricing.fees.application.command.rejecteFeePolicy.RejectFeePolicyResponse;
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
