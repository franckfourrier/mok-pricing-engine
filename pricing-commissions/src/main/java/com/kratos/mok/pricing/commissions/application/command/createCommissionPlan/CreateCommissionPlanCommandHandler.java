package com.kratos.mok.pricing.commissions.application.command.createCommissionPlan;

import com.kratos.mok.pricing.commissions.domain.CommissionPlan;
import com.kratos.mok.pricing.commissions.domain.CommissionTarget;
import com.kratos.mok.pricing.commissions.domain.enums.BeneficiaryType;
import com.kratos.mok.pricing.commissions.domain.event.CommissionPlanCreatedEvent;
import com.kratos.mok.pricing.commissions.domain.gateway.CommissionRegulatoryGatekeeper;
import com.kratos.mok.pricing.commissions.domain.repository.CommissionPlanRepository;
import com.kratos.mok.pricing.commissions.domain.strategy.CommissionStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.SubscriberDepositStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.DirectStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.SubscriberWithdrawalStrategy;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;
import com.kratos.mok.pricing.commissions.domain.vo.Percentage;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.event.ConfigurationBlockedEvent;
import com.kratos.mok.pricing.shared.domain.exception.ConflictException;
import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.RegulatoryViolationException;
import com.kratos.mok.pricing.shared.domain.time.TimeProvider;
import com.kratos.mok.pricing.shared.domain.vo.Priority;
import com.kratos.mok.pricing.shared.domain.vo.ValidityPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateCommissionPlanCommandHandler {

    private final CommissionPlanRepository repository;
    private final CommissionRegulatoryGatekeeper regulatoryGatekeeper;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeProvider timeProvider;

    @Transactional
    public CreateCommissionPlanResponse handle(CreateCommissionPlanCommand cmd, String authorId) {
        log.info("CreateCommissionPlan: transactionCode={}, type={}, scope={}, value={}",
                cmd.transactionCode(), cmd.type(), cmd.targetScope(), cmd.targetValue());

        var now = timeProvider.now();

        CommissionTarget target = toTarget(cmd.targetScope(), cmd.targetValue());
        CommissionStrategy strategy = toStrategy(cmd);

        ValidityPeriod validity = (cmd.validityStart() == null && cmd.validityEnd() == null)
                ? ValidityPeriod.PERMANENT
                : new ValidityPeriod(cmd.validityStart(), cmd.validityEnd());

        var priority = Priority.defaultFor(target.scope());

        CommissionPlan plan = CommissionPlan.draft(
                cmd.transactionCode(),
                cmd.type(),
                target,
                strategy,
                validity,
                priority,
                authorId,
                now
        );

        if (repository.existsConflictingPlan(plan)) {
            block(plan, authorId, "CONFLICTING_PLAN", "Plan conflict", cmd, now);
            throw new ConflictException("Un plan de commission actif existe déjà pour ce périmètre");
        }

        try {
            regulatoryGatekeeper.validate(plan.toComplianceData());
        } catch (RegulatoryViolationException e) {
            block(plan, authorId, e.regulationCode(), e.getMessage(), cmd , now);
            throw new DomainValidationException(
                    "BEAC_NON_COMPLIANT",
                    e.getMessage(),
                    Map.of("regulationCode", e.regulationCode())
            );
        }

        plan.submitForApproval(authorId, now, "SUBMIT_FOR_APPROVAL");
        repository.save(plan);

        eventPublisher.publishEvent(new CommissionPlanCreatedEvent(
                plan.id().value(),
                authorId,
                now
        ));

        return new CreateCommissionPlanResponse(plan.id().value(), true, plan.status());
    }

    private CommissionStrategy toStrategy(CreateCommissionPlanCommand cmd) {

        TransactionCode tc = cmd.transactionCode();

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
        // 1. Guard Clause : Gestion des cas aux limites (Null et Empty)
        if (raw == null || raw.trim().isEmpty()) {
            throw new DomainValidationException(
                    "INVALID_PERCENTAGE",
                    "Percentage cannot be null or empty",
                    Map.of("value", String.valueOf(raw))
            );
        }

        try {
            BigDecimal value = new BigDecimal(raw.trim());

            // 2. Validation métier : le pourcentage doit être compris entre 0% et 100% inclusivement
            if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new DomainValidationException(
                        "INVALID_PERCENTAGE",
                        "Percentage must be between 0 and 100 inclusive",
                        Map.of("value", raw, "received", value.toPlainString())
                );
            }

            // 3. Conversion : pourcentage humain → ratio décimal (0 à 1) avec échelle de 10
            BigDecimal decimalValue = value
                    .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

            return new Percentage(decimalValue);

        } catch (NumberFormatException e) {
            // 4. Capture de l'erreur de formatage de chaîne
            throw new DomainValidationException(
                    "INVALID_PERCENTAGE",
                    "Invalid number format for percentage: " + raw,
                    Map.of("value", raw)
            );
        }
    }

    private void block(
            CommissionPlan p,
            String actor,
            String code,
            String reason,
            CreateCommissionPlanCommand cmd,
            OffsetDateTime at
    ) {

        p.block(code, reason, actor, at);
        repository.save(p);

        eventPublisher.publishEvent(new ConfigurationBlockedEvent(
                p.id().value(),
                "COMMISSION_PLAN",
                actor,
                code,
                reason,
                Map.of(
                        "transactionCode", cmd.transactionCode().name(),
                        "transactionType", cmd.type().name(),
                        "scope", cmd.targetScope().name(),
                        "value", cmd.targetValue()
                ),
                at
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