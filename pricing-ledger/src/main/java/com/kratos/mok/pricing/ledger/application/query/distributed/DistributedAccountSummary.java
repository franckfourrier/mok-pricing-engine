package com.kratos.mok.pricing.ledger.application.query.distributed;

public record DistributedAccountSummary(
        String id,
        String actorType,
        long count,
        String distributedAmount
) {}


