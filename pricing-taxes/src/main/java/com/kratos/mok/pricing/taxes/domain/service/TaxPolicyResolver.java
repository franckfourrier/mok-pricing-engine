package com.kratos.mok.pricing.taxes.domain.service;

import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import com.kratos.mok.pricing.taxes.domain.TaxPolicy;
import com.kratos.mok.pricing.taxes.domain.enums.TaxPolicyStatus;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TaxPolicyResolver {

    public List<TaxPolicy> resolveActivePolicies(
            List<TaxPolicy> candidates,
            PricingRequestContext ctx,
            OffsetDateTime at
    ) {

        if (candidates == null || candidates.isEmpty()) {
            throw new NotFoundException(
                    "TAX_POLICY_NOT_FOUND",
                    "No tax policy configured for this transaction type / scope",
                    Map.of(
                            "transactionCode", ctx.transactionCode().name(),
                            "accountId", ctx.accountId()
                    )
            );
        }

        List<TaxPolicy> activePolicies = candidates.stream()
                .filter(p -> p.status() == TaxPolicyStatus.ACTIVE)
                .sorted(
                        Comparator.comparing(
                                (TaxPolicy p) -> p.created().timestamp(),
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                )
                .toList();

        if (activePolicies.isEmpty()) {
            throw new NotFoundException(
                    "TAX_POLICY_NOT_APPLICABLE",
                    "No ACTIVE tax policy applicable for the provided context",
                    Map.of(
                            "transactionCode", ctx.transactionCode().name(),
                            "accountId", ctx.accountId()
                    )
            );
        }

        return activePolicies;
    }

    public TaxPolicy resolveBestPolicy(List<TaxPolicy> candidates, PricingRequestContext ctx, OffsetDateTime at) {
        if (candidates == null || candidates.isEmpty()) {
            throw new NotFoundException(
                    "TAX_POLICY_NOT_FOUND",
                    "No tax policy configured for this transaction type / scope",
                    Map.of()
            );
        }

        return candidates.stream()
                // V1: on ne calcule que sur ACTIVE
                .filter(p -> p.status().name().equals("ACTIVE"))
                .sorted(Comparator.comparing(p -> p.created().timestamp(), Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "TAX_POLICY_NOT_APPLICABLE",
                        "No ACTIVE tax policy applicable for the provided context",
                        Map.of()
                ));
    }
}
