package com.kratos.mok.pricing.fees.application;

import com.kratos.mok.pricing.fees.application.adapter.FeeTransactionContextAdapter;
import com.kratos.mok.pricing.fees.application.port.FeeComputationPort;
import com.kratos.mok.pricing.fees.application.port.FeeComputationResult;
import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import com.kratos.mok.pricing.fees.domain.service.FeePolicyResolver;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FeeComputationService implements FeeComputationPort {

    private final FeePolicyRepository repository;
    private final FeePolicyResolver resolver;

    public FeeComputationService(FeePolicyRepository repository, FeePolicyResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Transactional(readOnly = true)
    @Override
    public FeeComputationResult computeFee(PricingRequestContext ctx) {

        var domainCtx = FeeTransactionContextAdapter.from(ctx);

        var candidates = repository.findCandidates(
                ctx.transactionType(),
                ctx.accountType().name(),
                ctx.accountId()
        );

        FeePolicy selected = resolver.resolveBestPolicy(candidates, domainCtx, LocalDateTime.now());

        var computation = selected.computeFee(ctx.amount(), domainCtx, LocalDateTime.now());

        return new FeeComputationResult(selected.id().toString(), computation.feeAmount());
    }
}
