package com.kratos.mok.pricing.app.infrastructure.rest.taxes.http.createTaxPolicy;

import com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto.createTaxPolicy.CreateTaxPolicyCommandMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.taxes.dto.createTaxPolicy.CreateTaxPolicyRequest;
import com.kratos.mok.pricing.taxes.application.command.createTaxPolicy.CreateTaxPolicyCommandHandler;
import com.kratos.mok.pricing.taxes.application.command.createTaxPolicy.CreateTaxPolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/tax-policies")
@RequiredArgsConstructor
public class CreateTaxPolicyCommandController {

    private final CreateTaxPolicyCommandHandler handler;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<CreateTaxPolicyResponse> create(
            @Valid @RequestBody CreateTaxPolicyRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String actor = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        var cmd = CreateTaxPolicyCommandMapper.toCommand(request);
        var res = handler.handle(cmd, actor);

        return ResponseEntity
                .created(URI.create("/v1/tax-policies/" + res.policyId()))
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

