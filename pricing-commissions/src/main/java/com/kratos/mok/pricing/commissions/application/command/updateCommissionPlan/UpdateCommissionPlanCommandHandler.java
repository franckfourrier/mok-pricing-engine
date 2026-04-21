package com.kratos.mok.pricing.commissions.application.command.updateCommissionPlan;

import com.kratos.mok.pricing.commissions.domain.CommissionPlan;
import com.kratos.mok.pricing.commissions.domain.enums.BeneficiaryType;
import com.kratos.mok.pricing.commissions.domain.event.CommissionPlanUpdatedEvent;
import com.kratos.mok.pricing.commissions.domain.gateway.CommissionRegulatoryGatekeeper;
import com.kratos.mok.pricing.commissions.domain.repository.CommissionPlanRepository;
import com.kratos.mok.pricing.commissions.domain.strategy.CommissionStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.SubscriberDepositStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.DirectStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.SubscriberWithdrawalStrategy;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionPlanId;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;
import com.kratos.mok.pricing.commissions.domain.vo.Percentage;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.event.ConfigurationBlockedEvent;
import com.kratos.mok.pricing.shared.domain.exception.ConflictException;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;
import com.kratos.mok.pricing.shared.domain.exception.RegulatoryViolationException;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateCommissionPlanCommandHandler {

    private final CommissionPlanRepository repository;
    private final CommissionRegulatoryGatekeeper regulatoryGatekeeper;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeProvider timeProvider;

    @Transactional
    public UpdateCommissionPlanResponse handle(UpdateCommissionPlanCommand cmd, String actor) {

        CommissionPlan plan = repository.findById(CommissionPlanId.from(cmd.commissionPlanId()))
                .orElseThrow(() -> new NotFoundException(
                        "COMMISSION_PLAN_NOT_FOUND",
                        "Commission plan not found",
                        Map.of("id", cmd.commissionPlanId())
                ));

        TransactionCode newTransactionCode = cmd.transactionCode();
        OffsetDateTime now = timeProvider.now();

        CommissionStrategy newStrategy = toStrategy(newTransactionCode, cmd);

        plan.updateConfiguration(
                newTransactionCode,
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

    private CommissionStrategy toStrategy(TransactionCode tc, UpdateCommissionPlanCommand cmd) {

        if (tc == TransactionCode.SUBSCRIBER_DEPOSIT) {
            var keys = requiredList(cmd.keys(), "keys are required for SUBSCRIBER DEPOSIT");
            return new SubscriberDepositStrategy(toShares(keys));
        }

        if (tc == TransactionCode.SUBSCRIBER_WITHDRAWAL) {
            var keys = requiredList(cmd.keys(), "keys are required for SUBSCRIBER WITHDRAWAL");
            return new SubscriberWithdrawalStrategy(toShares(keys));
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

            // ]0,100]
            if (v.compareTo(BigDecimal.ZERO) <= 0 || v.compareTo(new BigDecimal("100")) > 0) {
                throw new DomainValidationException(
                        "INVALID_PERCENTAGE",
                        "Le pourcentage doit être dans ]0,100] (ex: 1.5 pour 1.5%)",
                        Map.of("value", raw)
                );
            }

            // Conversion vers fraction interne (0 → 1)
            BigDecimal fraction = v.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);

            return new Percentage(fraction);

        } catch (NumberFormatException e) {
            throw new DomainValidationException(
                    "INVALID_PERCENTAGE",
                    "Format de pourcentage invalide : " + raw,
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
        OffsetDateTime now = timeProvider.now();
        plan.block(code, reason, actor, now);
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
                ),
                now
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
