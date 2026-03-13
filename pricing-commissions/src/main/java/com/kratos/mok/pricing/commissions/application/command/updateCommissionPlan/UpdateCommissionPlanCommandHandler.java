package com.kratos.mok.pricing.commissions.application.command.updateCommissionPlan;

import com.kratos.mok.pricing.commissions.domain.CommissionPlan;
import com.kratos.mok.pricing.commissions.domain.enums.BeneficiaryType;
import com.kratos.mok.pricing.commissions.domain.event.CommissionPlanUpdatedEvent;
import com.kratos.mok.pricing.commissions.domain.gateway.CommissionRegulatoryGatekeeper;
import com.kratos.mok.pricing.commissions.domain.repository.CommissionPlanRepository;
import com.kratos.mok.pricing.commissions.domain.strategy.CommissionStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.DepositDistributionStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.DirectStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.WithdrawalAgentKratosStrategy;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionPlanId;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;
import com.kratos.mok.pricing.commissions.domain.vo.Percentage;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.event.ConfigurationBlockedEvent;
import com.kratos.mok.pricing.shared.domain.exception.ConflictException;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;
import com.kratos.mok.pricing.shared.domain.exception.RegulatoryViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateCommissionPlanCommandHandler {

    private final CommissionPlanRepository repository;
    private final CommissionRegulatoryGatekeeper regulatoryGatekeeper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UpdateCommissionPlanResponse handle(UpdateCommissionPlanCommand cmd, String actor) {

        CommissionPlan plan = repository.findById(CommissionPlanId.from(cmd.commissionPlanId()))
                .orElseThrow(() -> new NotFoundException(
                        "COMMISSION_PLAN_NOT_FOUND",
                        "Commission plan not found",
                        Map.of("id", cmd.commissionPlanId())
                ));

        CommissionStrategy newStrategy = toStrategy(plan.transactionType(), cmd);
        var now = LocalDateTime.now();

        plan.updateConfiguration(
                newStrategy,
                plan.validity(),
                plan.priority(),
                actor,
                now,
                "UPDATE_AND_RESUBMIT"
        );

        if (repository.existsConflictingPlan(plan)) {
            block(plan, actor, "CONFLICTING_PLAN", "Plan conflict after update", cmd);
            throw new ConflictException("Un plan de commission actif existe déjà pour ce périmètre");
        }

        try {
            regulatoryGatekeeper.validate(plan.toComplianceData());
        } catch (RegulatoryViolationException e) {
            block(plan, actor, e.regulationCode(), e.getMessage(), cmd);
            throw new DomainValidationException(
                    "BEAC_NON_COMPLIANT",
                    e.getMessage(),
                    Map.of("regulationCode", e.regulationCode())
            );
        }

        repository.save(plan);

        eventPublisher.publishEvent(new CommissionPlanUpdatedEvent(
                plan.id().value(),
                actor,
                plan.status().name(),
                now
        ));

        return new UpdateCommissionPlanResponse(
                plan.id().value(),
                true,
                plan.status()
        );
    }

    private CommissionStrategy toStrategy(TransactionType tt, UpdateCommissionPlanCommand cmd) {

        if (tt == TransactionType.DEPOSIT) {
            var keys = requiredList(cmd.keys(), "keys are required for DEPOSIT");
            return new DepositDistributionStrategy(toShares(keys));
        }

        if (tt == TransactionType.WITHDRAWAL) {
            String agent = required(cmd.agentPercentage(), "agentPercentage is required for WITHDRAWAL");
            String cov = required(cmd.coverageRate(), "coverageRate is required for WITHDRAWAL");
            return new WithdrawalAgentKratosStrategy(
                    toPercentage(agent),
                    toPercentage(cov)
            );
        }

        var keys = requiredList(cmd.keys(), "keys are required for DIRECT strategy");
        return new DirectStrategy(toShares(keys));
    }

    private List<CommissionShare> toShares(List<KeyCommand> keys) {
        return keys.stream()
                .map(k -> new CommissionShare(
                        BeneficiaryType.valueOf(required(k.beneficiary(), "beneficiary is required").trim().toUpperCase()),
                        toPercentage(required(k.percentage(), "percentage is required"))
                ))
                .toList();
    }

    private Percentage toPercentage(String raw) {
        try {
            BigDecimal v = new BigDecimal(raw.trim());
            if (v.compareTo(BigDecimal.ONE) > 0) {
                v = v.divide(new BigDecimal("100"));
            }
            return new Percentage(v);
        } catch (Exception e) {
            throw new DomainValidationException(
                    "INVALID_PERCENTAGE",
                    "Invalid percentage: " + raw,
                    Map.of("value", raw)
            );
        }
    }

    private void block(
            CommissionPlan plan,
            String actor,
            String code,
            String reason,
            UpdateCommissionPlanCommand cmd
    ) {
        plan.block(code, reason, actor, LocalDateTime.now());
        repository.save(plan);

        eventPublisher.publishEvent(new ConfigurationBlockedEvent(
                plan.id().value(),
                "COMMISSION_PLAN",
                actor,
                code,
                reason,
                Map.of(
                        "commissionPlanId", cmd.commissionPlanId(),
                        "transactionCode", plan.transactionCode().name(),
                        "transactionType", plan.transactionType().name(),
                        "scope", plan.target().scope().name(),
                        "value", plan.target().value()
                )
        ));
    }

    private <T> List<T> requiredList(List<T> list, String msg) {
        if (list == null || list.isEmpty()) {
            throw new DomainValidationException("LIST_REQUIRED", msg, Map.of());
        }
        return list;
    }

    private String required(String v, String msg) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(msg);
        }
        return v;
    }
}
