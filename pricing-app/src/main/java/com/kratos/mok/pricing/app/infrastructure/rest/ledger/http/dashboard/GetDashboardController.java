package com.kratos.mok.pricing.app.infrastructure.rest.ledger.http.dashboard;

import com.kratos.mok.pricing.app.infrastructure.rest.ledger.dto.DashboardMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.ledger.dto.DashboardResponse;
import com.kratos.mok.pricing.ledger.application.query.dashboard.DashboardView;
import com.kratos.mok.pricing.ledger.application.query.dashboard.GetDashboardQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/ledger/dashboard")
@RequiredArgsConstructor
public class GetDashboardController {

    private final GetDashboardQueryHandler handler;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {

        String actor = (actorId != null && !actorId.isBlank())
                ? actorId.trim()
                : resolveActor(jwt);

        DashboardView view = handler.handle();

        return ResponseEntity.ok(
                DashboardMapper.toResponse(view)
        );
    }

    private String resolveActor(Jwt jwt) {
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