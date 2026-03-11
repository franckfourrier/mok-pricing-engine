package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.archiveFeePolicy;

import com.kratos.mok.pricing.fees.application.command.archiveFeePolicy.ArchiveFeePolicyCommand;
import com.kratos.mok.pricing.fees.application.command.archiveFeePolicy.ArchiveFeePolicyCommandHandler;
import com.kratos.mok.pricing.fees.application.command.archiveFeePolicy.ArchiveFeePolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/fee-policies")
@RequiredArgsConstructor
public class ArchiveFeePolicyCommandController {

    private final ArchiveFeePolicyCommandHandler handler;

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
            summary = "Archive a fee policy",
            description = "Logical deletion. Requires role: ADMIN or SUPER_ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Archived")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<ArchiveFeePolicyResponse> archive(
            @PathVariable String id,
            @Valid @RequestBody(required = false) ArchiveFeePolicyRequest body,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        String reason = (body == null) ? null : body.reason();

        var cmd = new ArchiveFeePolicyCommand(id, reason);
        return ResponseEntity.ok(handler.handle(cmd, actor));
    }

    public record ArchiveFeePolicyRequest(String reason) {}

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