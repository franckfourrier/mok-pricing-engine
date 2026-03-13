package com.kratos.mok.pricing.app.infrastructure.rest.commissions.http.updateCommissionPlan;

import com.kratos.mok.pricing.app.infrastructure.config.swagger.OpenApiConfig;
import com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto.updateCommissionPlan.UpdateCommissionPlanCommandMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto.updateCommissionPlan.UpdateCommissionPlanRequest;
import com.kratos.mok.pricing.commissions.application.command.updateCommissionPlan.UpdateCommissionPlanCommandHandler;
import com.kratos.mok.pricing.commissions.application.command.updateCommissionPlan.UpdateCommissionPlanResponse;
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
@RequestMapping("/v1/commission-policies")
@RequiredArgsConstructor
public class UpdateCommissionPlanCommandController {

    private final UpdateCommissionPlanCommandHandler handler;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
            summary = "Update a commission plan",
            description = "Updates commission values and resets status to PENDING_APPROVAL",
            security = { @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH) }
    )
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Commission plan not found")
    @ApiResponse(responseCode = "409", description = "Conflict with another commission plan")
    public ResponseEntity<UpdateCommissionPlanResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateCommissionPlanRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        var cmd = UpdateCommissionPlanCommandMapper.toCommand(id, request);
        return ResponseEntity.ok(handler.handle(cmd, actor));
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