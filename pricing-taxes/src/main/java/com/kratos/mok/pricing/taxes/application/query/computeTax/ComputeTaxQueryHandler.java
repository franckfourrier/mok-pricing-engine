package com.kratos.mok.pricing.taxes.application.query.computeTax;

import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import com.kratos.mok.pricing.taxes.application.port.ComputeTaxQuery;
import com.kratos.mok.pricing.taxes.application.port.TaxComputationResult;
import com.kratos.mok.pricing.taxes.application.port.TaxLine;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.repository.TaxPolicyRepository;
import com.kratos.mok.pricing.taxes.domain.service.TaxPolicyResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

        List<TaxPolicy> activePolicies =
                resolver.resolveActivePolicies(
                        candidates,
                        ctx,
                        ctx.occurredAt()
                );

        List<TaxLine> lines = new ArrayList<>();

        Money totalTax = Money.of(
                BigDecimal.ZERO,
                ctx.amount().currency()
        );

        for (TaxPolicy policy : activePolicies) {

            Money taxAmount = policy.computeTax(ctx.amount());

            if (taxAmount != null && !taxAmount.isZero()) {

                totalTax = totalTax.add(taxAmount);

                lines.add(
                        new TaxLine(
                                policy.id().value(),
                                policy.strategy().type(),
                                policy.mode(),
                                taxAmount
                        )
                );
            }
        }

        return new TaxComputationResult(
                totalTax,
                lines
        );
    }
}