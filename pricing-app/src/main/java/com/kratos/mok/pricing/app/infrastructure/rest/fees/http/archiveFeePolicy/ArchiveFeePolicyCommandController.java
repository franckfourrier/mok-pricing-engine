package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.archiveFeePolicy;

import com.kratos.mok.pricing.app.infrastructure.rest.fees.dto.ArchiveFeePolicyRequest;
import com.kratos.mok.pricing.app.infrastructure.security.actor.CurrentActor;
import com.kratos.mok.pricing.fees.application.command.archiveFeePolicy.ArchiveFeePolicyCommand;
import com.kratos.mok.pricing.fees.application.command.archiveFeePolicy.ArchiveFeePolicyCommandHandler;
import com.kratos.mok.pricing.fees.application.command.archiveFeePolicy.ArchiveFeePolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @CurrentActor String actor
    ) {
        String reason = (body == null) ? null : body.reason();
        var cmd = new ArchiveFeePolicyCommand(id, reason);

        return ResponseEntity.ok(handler.handle(cmd, actor));
    }
}