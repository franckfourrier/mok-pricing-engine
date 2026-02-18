package com.kratos.mok.pricing.app.infrastructure.rest.commissions.http.createCommissionPlan;

import com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto.CreateCommissionPlanCommandMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.commissions.dto.CreateCommissionPlanRequest;
import com.kratos.mok.pricing.commissions.application.command.createCommissionPlan.CreateCommissionPlanCommandHandler;
import com.kratos.mok.pricing.commissions.application.command.createCommissionPlan.CreateCommissionPlanResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/commission-policies")
@RequiredArgsConstructor
public class CreateCommissionPlanCommandController {

    private final CreateCommissionPlanCommandHandler handler;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<CreateCommissionPlanResponse> create(
            @Valid @RequestBody CreateCommissionPlanRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String authorId = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        var cmd = CreateCommissionPlanCommandMapper.toCommand(request);
        var res = handler.handle(cmd, authorId);

        return ResponseEntity
                .created(URI.create("/v1/commission-plans/" + res.commissionPlanId()))
                .body(res);
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
