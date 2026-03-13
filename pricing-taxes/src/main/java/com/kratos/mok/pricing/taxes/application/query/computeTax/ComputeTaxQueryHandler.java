package com.kratos.mok.pricing.taxes.application.query.computeTax;

import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import com.kratos.mok.pricing.taxes.application.port.ComputeTaxQuery;
import com.kratos.mok.pricing.taxes.application.port.TaxComputationResult;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.repository.TaxPolicyRepository;
import com.kratos.mok.pricing.taxes.domain.service.TaxPolicyResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ComputeTaxQueryHandler implements ComputeTaxQuery {

    private final TaxPolicyRepository repository;
    private final TaxPolicyResolver resolver;

    public ComputeTaxQueryHandler(TaxPolicyRepository repository, TaxPolicyResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public TaxComputationResult computeTax(PricingRequestContext ctx) {

        var candidates = repository.findCandidates(
                ctx.accountType().name(),
                ctx.accountId(),
                ctx.transactionCode()
        );

        TaxPolicy selected = resolver.resolveBestPolicy(candidates, ctx, ctx.occurredAt());

        var tax = selected.computeTax(ctx.amount());

        return new TaxComputationResult(
                selected.id().value(),
                tax,
                selected.mode(),
                selected.strategy().type()
        );
    }
}