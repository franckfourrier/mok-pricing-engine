package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.createFeePolicy;

import com.kratos.mok.pricing.app.infrastructure.rest.fees.dto.CreateFeePolicyCommandMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.fees.dto.CreateFeePolicyRequest;
import com.kratos.mok.pricing.fees.application.command.createFeePolicy.CreateFeePolicyHandler;
import com.kratos.mok.pricing.fees.application.command.createFeePolicy.CreateFeePolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/fee-policies")
@RequiredArgsConstructor
public class CreateFeePolicyCommandController {

    private final CreateFeePolicyHandler handler;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<CreateFeePolicyResponse> create(
            @Valid @RequestBody CreateFeePolicyRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        String authorId = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveAuthorId(jwt);

        var cmd = CreateFeePolicyCommandMapper.toCommand(request);
        var res = handler.handle(cmd, authorId);

        return ResponseEntity
                .created(URI.create("/v1/fee-policies/" + res.policyId()))
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

