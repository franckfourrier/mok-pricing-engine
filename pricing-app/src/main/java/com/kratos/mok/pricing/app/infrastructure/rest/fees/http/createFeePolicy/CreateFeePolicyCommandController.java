package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.createFeePolicy;

import com.kratos.mok.pricing.app.infrastructure.rest.fees.dto.CreateFeePolicyCommandMapper;
import com.kratos.mok.pricing.app.infrastructure.rest.fees.dto.CreateFeePolicyRequest;
import com.kratos.mok.pricing.app.infrastructure.security.actor.AuthenticatedActorResolver;
import com.kratos.mok.pricing.app.infrastructure.security.actor.CurrentActor;
import com.kratos.mok.pricing.fees.application.command.createFeePolicy.CreateFeePolicyCommandHandler;
import com.kratos.mok.pricing.fees.application.command.createFeePolicy.CreateFeePolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/fee-policies")
@RequiredArgsConstructor
public class CreateFeePolicyCommandController {

    private final CreateFeePolicyCommandHandler handler;
    private final AuthenticatedActorResolver authenticatedActorResolver;

@PostMapping
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public ResponseEntity<CreateFeePolicyResponse> create(
        @Valid @RequestBody CreateFeePolicyRequest request,
        @CurrentActor String actor
    ) {

        var cmd = CreateFeePolicyCommandMapper.toCommand(request);

        var res = handler.handle(cmd, actor);

        return ResponseEntity
                .created(URI.create("/v1/fee-policies/" + res.policyId()))
                .body(res);
    }
}