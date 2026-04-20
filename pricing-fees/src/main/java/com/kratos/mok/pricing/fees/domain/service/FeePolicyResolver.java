package com.kratos.mok.pricing.fees.domain.service;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.TransactionContext;
import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FeePolicyResolver {

    public FeePolicy resolveBestPolicy(List<FeePolicy> candidates, TransactionContext ctx, OffsetDateTime now) {
        if (candidates == null || candidates.isEmpty()) {
            throw new NotFoundException(
                    "FEE_POLICY_NOT_FOUND",
                    "No fee policy configured for this transaction type / scope",
                    Map.of(
                            //"transactionType", ctx.transactionType().name(),
                            //"accountType", ctx.accountType(),
                            //"accountId", ctx.accountId()
                    )
            );
        }

        var at = (now == null) ? OffsetDateTime.now(ZoneOffset.UTC) : now;

        return candidates.stream()
                .filter(p -> p.isApplicableAt(at, ctx))
                .sorted(Comparator
                        .comparingInt((FeePolicy p) -> p.priority().value()).reversed()
                        .thenComparing(p -> p.created().timestamp(), Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "FEE_POLICY_NOT_APPLICABLE",
                        "No fee policy applicable for the provided context",
                        Map.of(
                                //"transactionType", ctx.transactionType().name(),
                                //"accountType", ctx.accountType(),
                                //"accountId", ctx.accountId(),
                                //"kycValidated", ctx.kycValidated(),
                                //"monthlyTxCount", ctx.monthlyTxCount(),
                                //"occurredAt", at.toString()
                        )
                ));
    }
}
