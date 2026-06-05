package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.rejectFeePolicy;

import com.kratos.mok.pricing.app.infrastructure.rest.fees.dto.RejectFeePolicyRequest;
import com.kratos.mok.pricing.app.infrastructure.security.actor.CurrentActor;
import com.kratos.mok.pricing.fees.application.command.rejectFeePolicy.RejectFeePolicyCommand;
import com.kratos.mok.pricing.fees.application.command.rejectFeePolicy.RejectFeePolicyCommandHandler;
import com.kratos.mok.pricing.fees.application.command.rejectFeePolicy.RejectFeePolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/fee-policies")
@RequiredArgsConstructor
public class RejectFeePolicyCommandController {

    private final RejectFeePolicyCommandHandler handler;

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @Operation(
            summary = "Reject a fee policy",
            description = "Requires role: SUPER_ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Rejected")
    @ApiResponse(responseCode = "403", description = "Forbidden (missing role SUPER_ADMIN)")
    public ResponseEntity<RejectFeePolicyResponse> reject(
            @PathVariable String id,
            @Valid @RequestBody(required = false) RejectFeePolicyRequest body,
            @CurrentActor String actor
    ) {
        String reason = (body == null) ? null : body.reason();
        var cmd = new RejectFeePolicyCommand(id, reason);

        return ResponseEntity.ok(handler.handle(cmd, actor));
    }
}