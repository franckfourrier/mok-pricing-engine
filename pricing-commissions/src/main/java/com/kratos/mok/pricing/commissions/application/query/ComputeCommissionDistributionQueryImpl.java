package com.kratos.mok.pricing.commissions.application.query;

import com.kratos.mok.pricing.commissions.application.port.ComputeCommissionDistributionQuery;
import com.kratos.mok.pricing.commissions.domain.CommissionPlan;
import com.kratos.mok.pricing.commissions.domain.enums.BeneficiaryType;
import com.kratos.mok.pricing.commissions.domain.repository.CommissionPlanRepository;
import com.kratos.mok.pricing.commissions.domain.strategy.*;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;
import com.kratos.mok.pricing.commissions.domain.vo.Percentage;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComputeCommissionDistributionQueryImpl implements ComputeCommissionDistributionQuery {

    private final CommissionPlanRepository repository;

    @Override
    public CommissionDistributionResult compute(PricingRequestContext ctx, Money commissionBase) {
        if (ctx == null) throw new IllegalArgumentException("ctx is required");
        if (commissionBase == null) throw new IllegalArgumentException("commissionBase is required");

        var candidates = repository.findCandidates(ctx.transactionType(),
                        ctx.accountType() == null ? null : ctx.accountType().name(),
                        ctx.accountId());

        CommissionPlan plan = candidates.stream().findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "COMMISSION_PLAN_NOT_FOUND",
                        "No approved commission plan found",
                        Map.of(
                                "transactionType", ctx.transactionType().name(),
                                "accountType", String.valueOf(ctx.accountType()),
                                "accountId", ctx.accountId()
                        )
                ));

        var lines = computeLines(ctx.transactionType(), plan.strategy(), commissionBase);

        // garde seulement les lignes non null / non zero
        lines = lines.stream()
                .filter(l -> l.amount() != null && !l.amount().isZero())
                .toList();

        return new CommissionDistributionResult(plan.id().value(), lines);
    }

    private List<CommissionDistributionResult.Line> computeLines(TransactionType txType,
                                                                 CommissionStrategy strategy,
                                                                 Money base) {

        if (strategy == null) throw new IllegalArgumentException("strategy is required");

        // base amount * percentage (scale Money)
        if (strategy instanceof DepositDistributionStrategy s) {
            ensure(txType == TransactionType.DEPOSIT, "DEPOSIT strategy used for non-DEPOSIT");
            return distributeShares(s.keys(), base, true); // pas de KRATOS attendu (on le filtre)
        }

        if (strategy instanceof DirectStrategy s) {
            return distributeShares(s.keys(), base, false);
        }

        if (strategy instanceof WithdrawalAgentKratosStrategy s) {
            ensure(txType == TransactionType.WITHDRAWAL, "WITHDRAWAL strategy used for non-WITHDRAWAL");

            Percentage agent = s.agentShare();
            Percentage coverage = s.coverageRate();
            Percentage kratos = s.kratosShare();

            // règles métier de compatibilité (sans refactor) :
            // 0 <= agent <= 1
            // 0 <= coverage <= 1
            // agent + coverage <= 1
            if (agent.value().add(coverage.value()).compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("Invalid withdrawal plan: agentShare + coverageRate > 1");
            }

            List<CommissionDistributionResult.Line> lines = new ArrayList<>();
            lines.add(line(BeneficiaryType.AGENT, agent, base));
            lines.add(line(BeneficiaryType.KRATOS, kratos, base));
            return lines;
        }

        throw new IllegalArgumentException("Unsupported commission strategy: " + strategy.getClass().getSimpleName());
    }

    private List<CommissionDistributionResult.Line> distributeShares(List<CommissionShare> shares, Money base, boolean rejectKratos) {
        if (shares == null || shares.isEmpty()) throw new IllegalArgumentException("shares is required");

        BigDecimal sum = shares.stream()
                .map(s -> s.share().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Invalid commission shares: sum > 1.0");
        }

        List<CommissionDistributionResult.Line> lines = new ArrayList<>();

        for (CommissionShare s : shares) {
            if (rejectKratos && s.beneficiaryType() == BeneficiaryType.KRATOS) {
                throw new IllegalArgumentException("DEPOSIT plan must not include KRATOS as beneficiary");
            }
            lines.add(line(s.beneficiaryType(), s.share(), base));
        }

        return lines;
    }

    private CommissionDistributionResult.Line line(BeneficiaryType b, Percentage p, Money base) {
        Money amount = multiply(base, p);
        return new CommissionDistributionResult.Line(b.name(), p.value().toPlainString(), amount);
    }

    private Money multiply(Money base, Percentage p) {
        // Money.of(BigDecimal, currency) supposé exister via Money.amount() etc.
        var v = base.amount().multiply(p.value());
        // On garde la même devise, scale Money gère le rounding interne.
        return new Money(v, base.currency());
    }

    private void ensure(boolean ok, String msg) {
        if (!ok) throw new IllegalArgumentException(msg);
    }
}
