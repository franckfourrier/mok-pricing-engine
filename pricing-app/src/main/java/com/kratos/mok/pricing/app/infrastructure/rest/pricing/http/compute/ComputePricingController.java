package com.kratos.mok.pricing.app.infrastructure.rest.pricing.http.compute;

import com.kratos.mok.pricing.app.application.query.computePricing.ComputePricingQueryHandler;
import com.kratos.mok.pricing.fees.application.query.computeFee.ComputeFeeQueryHandler;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import com.kratos.mok.pricing.taxes.application.query.computeTax.ComputeTaxQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pricing")
@RequiredArgsConstructor
public class ComputePricingController {

    private final ComputePricingQueryHandler handler;
    private final TimeProvider timeProvider;

    @Operation(
            summary = "Compute applicable fee and tax",
            description = "Returns the applicable fee and tax only (no commissions)"
    )
    @GetMapping("/compute")
    public ComputePricingResponse compute(@Valid @ModelAttribute ComputePricingRequest request) {

        var ctx = request.toDomainContext(timeProvider.now());

        var result = handler.handle(ctx);

        return ComputePricingResponse.from(request, result);
    }
}

/*
@RestController
@RequestMapping("/v1/pricing")
public class ComputePricingController {

    private final ComputeFeeQueryHandler feeService;
    private final ComputeTaxQueryHandler taxService;
    private final TimeProvider timeProvider;

    public ComputePricingController(ComputeFeeQueryHandler feeService, ComputeTaxQueryHandler taxService, TimeProvider timeProvider) {
        this.feeService = feeService;
        this.taxService = taxService;
        this.timeProvider = timeProvider;
    }

    @Operation(
            summary = "Compute applicable fee and tax",
            description = "Returns the applicable fee and tax only (no commissions)"
    )
    @GetMapping("/compute")
    public ComputePricingResponse compute(@Valid @ModelAttribute ComputePricingRequest request) {
        // 1. On transforme l'entrée en objet du Domaine
        var ctx = request.toDomainContext(timeProvider.now());

        // 2. On exécute la logique métier
        var feeResult = feeService.computeFee(ctx);
        var taxResult = taxService.computeTax(ctx);

        // 3. On transforme le résultat du domaine en réponse API
        return ComputePricingResponse.from(request, feeResult, taxResult);
    }
}*/
