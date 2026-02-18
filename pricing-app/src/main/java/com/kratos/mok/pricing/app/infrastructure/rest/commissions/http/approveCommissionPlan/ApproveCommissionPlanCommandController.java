package com.kratos.mok.pricing.app.infrastructure.rest.commissions.http.approveCommissionPlan;

import com.kratos.mok.pricing.commissions.application.command.approveCommissionPlan.ApproveCommissionPlanCommand;
import com.kratos.mok.pricing.commissions.application.command.approveCommissionPlan.ApproveCommissionPlanCommandHandler;
import com.kratos.mok.pricing.commissions.application.command.approveCommissionPlan.ApproveCommissionPlanResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/commission-policies")
@RequiredArgsConstructor
public class ApproveCommissionPlanCommandController {

    private final ApproveCommissionPlanCommandHandler handler;

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<ApproveCommissionPlanResponse> approve(
            @PathVariable String id,
            @Valid @RequestBody(required = false) ApproveCommissionPlanRequest body,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        String reason = (body == null) ? null : body.reason();

        var cmd = new ApproveCommissionPlanCommand(id, reason);
        return ResponseEntity.ok(handler.handle(cmd, actor));
    }

    public record ApproveCommissionPlanRequest(String reason) {}

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
