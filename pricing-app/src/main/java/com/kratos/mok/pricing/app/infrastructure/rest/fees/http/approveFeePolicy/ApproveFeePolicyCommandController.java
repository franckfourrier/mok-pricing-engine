package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.approveFeePolicy;

import com.kratos.mok.pricing.app.infrastructure.security.actor.CurrentActor;
import com.kratos.mok.pricing.fees.application.command.approveFeePolicy.ApproveFeePolicyCommand;
import com.kratos.mok.pricing.fees.application.command.approveFeePolicy.ApproveFeePolicyCommandHandler;
import com.kratos.mok.pricing.fees.application.command.approveFeePolicy.ApproveFeePolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/fee-policies")
@RequiredArgsConstructor
public class ApproveFeePolicyCommandController {

    private final ApproveFeePolicyCommandHandler handler;

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @Operation(
            summary = "Approve a fee policy",
            description = "Requires role: SUPER_ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Approve")
    @ApiResponse(responseCode = "403", description = "Forbidden (missing role SUPER_ADMIN)")
    public ResponseEntity<ApproveFeePolicyResponse> approve(
            @PathVariable String id,
            @CurrentActor String actor
    ) {

        var cmd = new ApproveFeePolicyCommand(id);

        return ResponseEntity.ok(
                handler.handle(cmd, actor)
        );
    }
}