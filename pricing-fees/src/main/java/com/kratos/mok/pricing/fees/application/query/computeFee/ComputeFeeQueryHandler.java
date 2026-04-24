package com.kratos.mok.pricing.fees.application.query.computeFee;

import com.kratos.mok.pricing.fees.application.adapter.FeeTransactionContextAdapter;
import com.kratos.mok.pricing.fees.application.port.ComputeFeeQuery;
import com.kratos.mok.pricing.fees.application.port.FeeComputationResult;
import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.service.FeePolicyResolver;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ComputeFeeQueryHandler implements ComputeFeeQuery {

    private final FeePolicyRepository repository;
    private final FeePolicyResolver resolver;
    private final TimeProvider timeProvider;

    public ComputeFeeQueryHandler(FeePolicyRepository repository, FeePolicyResolver resolver, TimeProvider timeProvider) {
        this.repository = repository;
        this.resolver = resolver;
        this.timeProvider = timeProvider;
    }

    @Override
    public FeeComputationResult computeFee(PricingRequestContext ctx) {

        var now = timeProvider.now();

        var domainCtx = FeeTransactionContextAdapter.from(ctx);

        var candidates = repository.findCandidates(
                ctx.transactionCode(),
                ctx.accountType().name(),
                ctx.accountId()
        );

        FeePolicy selected = resolver.resolveBestPolicy(candidates, domainCtx, now);

        var computation = selected.computeFee(ctx.amount(), domainCtx, now);

        return new FeeComputationResult(selected.id().value(), computation.feeAmount());
    }
}
