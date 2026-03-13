package com.kratos.mok.pricing.app.infrastructure.rest.taxes.http.updateTaxPolicy;

import com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto.updateTaxPolicy.UpdateTaxPolicyCommandMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto.updateTaxPolicy.UpdateTaxPolicyRequest;
import com.kratos.mok.pricing.taxes.application.command.updateTaxPolicy.UpdateTaxPolicyCommandHandler;
import com.kratos.mok.pricing.taxes.application.command.updateTaxPolicy.UpdateTaxPolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class UpdateTaxPolicyCommandController {

    private final UpdateTaxPolicyCommandHandler handler;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
            summary = "Update a tax policy",
            description = "Updates tax policy and resets status to PENDING_APPROVAL"
    )
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Tax policy not found")
    @ApiResponse(responseCode = "409", description = "Conflict with another tax policy")
    public ResponseEntity<UpdateTaxPolicyResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateTaxPolicyRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        var cmd = UpdateTaxPolicyCommandMapper.toCommand(id, request);
        var res = handler.handle(cmd, actor);

        return ResponseEntity.ok(res);
    }

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