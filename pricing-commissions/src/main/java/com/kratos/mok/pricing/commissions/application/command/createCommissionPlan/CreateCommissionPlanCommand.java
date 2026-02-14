package com.kratos.mok.pricing.commissions.application.command.createCommissionPlan;

import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public record CreateCommissionPlanCommand(
        TransactionType type,
        TargetScope targetScope,
        String targetValue,

        // DEPOSIT_DISTRIBUTION / DIRECT
        List<KeyCommand> keys,

        // WITHDRAWAL_AGENT_KRATOS
        String agentPercentage,   // ex: "40" ou "0.40"
        String coverageRate,      // ex: "10" ou "0.10"

        LocalDateTime validityStart,
        LocalDateTime validityEnd
) {}
