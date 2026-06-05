package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.updateFeePolicy;

import com.kratos.mok.pricing.app.infrastructure.rest.fees.dto.UpdateFeePolicyCommandMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.fees.dto.UpdateFeePolicyRequest;
import com.kratos.mok.pricing.app.infrastructure.security.actor.CurrentActor;
import com.kratos.mok.pricing.fees.application.command.updateFeePolicy.UpdateFeePolicyCommandHandler;
import com.kratos.mok.pricing.fees.application.command.updateFeePolicy.UpdateFeePolicyResponse;
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
public class UpdateFeePolicyCommandController {

    private final UpdateFeePolicyCommandHandler handler;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
            summary = "Update a fee policy",
            description = "Updates fee policy and resets status to PENDING_APPROVAL"
    )
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<UpdateFeePolicyResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateFeePolicyRequest request,
            @CurrentActor String actor
    ) {

        var cmd = UpdateFeePolicyCommandMapper.toCommand(id, request);

        return ResponseEntity.ok(
                handler.handle(cmd, actor)
        );
    }
}