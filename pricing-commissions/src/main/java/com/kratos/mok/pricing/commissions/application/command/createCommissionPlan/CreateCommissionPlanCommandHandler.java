package com.kratos.mok.pricing.commissions.application.command.createCommissionPlan;

import com.kratos.mok.pricing.commissions.domain.CommissionPlan;
import com.kratos.mok.pricing.commissions.domain.CommissionTarget;
import com.kratos.mok.pricing.commissions.domain.enums.BeneficiaryType;
import com.kratos.mok.pricing.commissions.domain.event.CommissionPlanCreatedEvent;
import com.kratos.mok.pricing.commissions.domain.gateway.CommissionRegulatoryGatekeeper;
import com.kratos.mok.pricing.commissions.domain.repository.CommissionPlanRepository;
import com.kratos.mok.pricing.commissions.domain.strategy.*;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;
import com.kratos.mok.pricing.commissions.domain.vo.Percentage;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.event.ConfigurationBlockedEvent;
import com.kratos.mok.pricing.shared.domain.exception.ConflictException;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.RegulatoryViolationException;
import com.kratos.mok.pricing.shared.domain.vo.Priority;
import com.kratos.mok.pricing.shared.domain.vo.ValidityPeriod;
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
public class CreateCommissionPlanCommandHandler {

    private final CommissionPlanRepository repository;
    private final CommissionRegulatoryGatekeeper regulatoryGatekeeper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateCommissionPlanResponse handle(CreateCommissionPlanCommand cmd, String authorId) {

        log.info("CreateCommissionPlan: type={}, scope={}, value={}",
                cmd.type(), cmd.targetScope(), cmd.targetValue());

        CommissionTarget target = toTarget(cmd.targetScope(), cmd.targetValue());
        CommissionStrategy strategy = toStrategy(cmd);

        ValidityPeriod validity = (cmd.validityStart() == null && cmd.validityEnd() == null)
                ? ValidityPeriod.PERMANENT
                : new ValidityPeriod(cmd.validityStart(), cmd.validityEnd());

        var priority = Priority.defaultFor(target.scope());

        CommissionPlan plan = CommissionPlan.draft(
                cmd.type(),
                target,
                strategy,
                validity,
                priority,
                authorId,
                LocalDateTime.now()
        );

        // Barrière 2 — conflit = BLOCK
        if (repository.existsConflictingPlan(plan)) {
            block(plan, authorId, "CONFLICTING_PLAN", "Plan conflict", cmd);
            throw new ConflictException("Un plan de commission actif existe déjà pour ce périmètre");
        }

        // Barrière 3 — conformité (optionnel)
        try {
            regulatoryGatekeeper.validate(plan.toComplianceData());
        } catch (RegulatoryViolationException e) {
            block(plan, authorId, e.regulationCode(), e.getMessage(), cmd);
            throw new DomainValidationException(
                    "BEAC_NON_COMPLIANT",
                    e.getMessage(),
                    Map.of("regulationCode", e.regulationCode())
            );
        }

        plan.submitForApproval(authorId, LocalDateTime.now(), "SUBMIT_FOR_APPROVAL");
        repository.save(plan);

        eventPublisher.publishEvent(new CommissionPlanCreatedEvent(
                plan.id().value(), authorId, LocalDateTime.now()
        ));

        return new CreateCommissionPlanResponse(plan.id().value(), true, plan.status());
    }

    private CommissionStrategy toStrategy(CreateCommissionPlanCommand cmd) {

        TransactionType tt = cmd.type();

        // 1) DEPOSIT => DepositDistributionStrategy(keys)
        if (tt == TransactionType.DEPOSIT) {
            var keys = requiredList(cmd.keys(), "keys are required for DEPOSIT");
            return new DepositDistributionStrategy(toShares(keys));
        }

        // 2) WITHDRAWAL => WithdrawalAgentKratosStrategy(agentShare, coverageRate)
        if (tt == TransactionType.WITHDRAWAL) {
            String agent = required(cmd.agentPercentage(), "agentPercentage is required for WITHDRAWAL");
            String cov = required(cmd.coverageRate(), "coverageRate is required for WITHDRAWAL");
            return new WithdrawalAgentKratosStrategy(
                    toPercentage(agent),
                    toPercentage(cov)
            );
        }

        // 3) default => DIRECT(keys)
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
            if (v.compareTo(BigDecimal.ONE) > 0) v = v.divide(new BigDecimal("100"));
            return new Percentage(v);
        } catch (Exception e) {
            throw new DomainValidationException(
                    "INVALID_PERCENTAGE",
                    "Invalid percentage: " + raw,
                    Map.of("value", raw)
            );
        }
    }

    private void block(CommissionPlan p, String actor, String code, String reason, CreateCommissionPlanCommand cmd) {
        p.block(code, reason, actor, LocalDateTime.now());
        repository.save(p);

        eventPublisher.publishEvent(new ConfigurationBlockedEvent(
                p.id().value(),
                "COMMISSION_PLAN",
                actor,
                code,
                reason,
                Map.of(
                        "transactionType", cmd.type().name(),
                        "scope", cmd.targetScope().name(),
                        "value", cmd.targetValue()
                )
        ));
    }

    private CommissionTarget toTarget(TargetScope scope, String value) {
        String v = required(value, "targetValue is required").trim();
        return switch (scope) {
            case GLOBAL -> CommissionTarget.global();
            case ACCOUNT_TYPE -> CommissionTarget.accountType(v);
            case ACCOUNT_ID -> CommissionTarget.accountId(v);
        };
    }

    private <T> List<T> requiredList(List<T> list, String msg) {
        if (list == null || list.isEmpty()) {
            throw new DomainValidationException("LIST_REQUIRED", msg, Map.of());
        }
        return list;
    }

    private String required(String v, String msg) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(msg);
        return v;
    }
}
