package com.kratos.mok.pricing.commissions.application.query;

import com.kratos.mok.pricing.commissions.application.port.ComputeCommissionDistributionQuery;
import com.kratos.mok.pricing.commissions.domain.CommissionPlan;
import com.kratos.mok.pricing.commissions.domain.enums.BeneficiaryType;
import com.kratos.mok.pricing.commissions.domain.repository.CommissionPlanRepository;
import com.kratos.mok.pricing.commissions.domain.strategy.CommissionStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.SubscriberDepositStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.DirectStrategy;
import com.kratos.mok.pricing.commissions.domain.strategy.SubscriberWithdrawalStrategy;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;
import com.kratos.mok.pricing.commissions.domain.vo.Percentage;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.kratos.mok.pricing.shared.domain.enums.TransactionCode.SUBSCRIBER_DEPOSIT;
import static com.kratos.mok.pricing.shared.domain.enums.TransactionCode.SUBSCRIBER_WITHDRAWAL;

@Service
@RequiredArgsConstructor
public class ComputeCommissionDistributionQueryImpl implements ComputeCommissionDistributionQuery {

    private final CommissionPlanRepository repository;

    @Override
    public CommissionDistributionResult compute(PricingRequestContext ctx, Money commissionBase) {
        if (ctx == null) throw new IllegalArgumentException("ctx is required");
        if (commissionBase == null) throw new IllegalArgumentException("commissionBase is required");

        var candidates = repository.findCandidates(
                ctx.transactionCode(),
                ctx.accountType() == null ? null : ctx.accountType().name(),
                ctx.accountId()
        );

        CommissionPlan plan = candidates.stream().findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "COMMISSION_PLAN_NOT_FOUND",
                        "No approved commission plan found",
                        Map.of(
                                "transactionCode", ctx.transactionCode().name(),
                                "accountType", String.valueOf(ctx.accountType()),
                                "accountId", ctx.accountId()
                        )
                ));

        var lines = computeLines(ctx.transactionCode(), ctx, plan.strategy(), commissionBase);

        lines = lines.stream()
                .filter(l -> l.amount() != null && !l.amount().isZero())
                .toList();

        return new CommissionDistributionResult(plan.id().value(), lines);
    }

    private List<CommissionDistributionResult.Line> computeLines(
            TransactionCode txCode,
            PricingRequestContext ctx,
            CommissionStrategy strategy,
            Money base
    ) {
        if (strategy == null) throw new IllegalArgumentException("strategy is required");

        if (strategy instanceof SubscriberDepositStrategy s) {
            ensure(txCode == SUBSCRIBER_DEPOSIT, "DEPOSIT strategy used for non-DEPOSIT");
            return distributeShares(s.keys(), ctx, base, true);
        }

        if (strategy instanceof DirectStrategy s) {
            return distributeShares(s.keys(), ctx, base, false);
        }

        if (strategy instanceof SubscriberWithdrawalStrategy s) {
            ensure(txCode == SUBSCRIBER_WITHDRAWAL, "WITHDRAWAL strategy used for non-WITHDRAWAL");

            List<CommissionDistributionResult.Line> lines = new ArrayList<>();

            Percentage agentWithdrawal = findShare(s.keys(), BeneficiaryType.AGENT);

            // 2. Charger stratégie DEPOSIT
            CommissionPlan depositPlan = repository.findCandidates(
                            SUBSCRIBER_DEPOSIT,
                            ctx.accountType() == null ? null : ctx.accountType().name(),
                            ctx.accountId()
                    ).stream().findFirst()
                    .orElseThrow(() -> new NotFoundException(
                            "DEPOSIT_PLAN_NOT_FOUND",
                            "Deposit plan required for withdrawal strategy",
                            Map.of("accountId", ctx.accountId())
                    ));

            if (!(depositPlan.strategy() instanceof SubscriberDepositStrategy depositStrategy)) {
                throw new IllegalStateException("Deposit plan must use SubscriberDepositStrategy");
            }

            // 3. Récupérer AGENT + DISTRIBUTOR + SUPER_DISTRIBUTOR
            Percentage agentDeposit = findShare(depositStrategy.keys(), BeneficiaryType.DISTRIBUTOR);
            Percentage distributor = findShare(depositStrategy.keys(), BeneficiaryType.DISTRIBUTOR);
            Percentage superDistributor = findShare(depositStrategy.keys(), BeneficiaryType.SUPER_DISTRIBUTOR);

            // 4. Calcul KRATOS
            BigDecimal sum = agentWithdrawal.value()
                    .add(agentDeposit.value())
                    .add(distributor.value())
                    .add(superDistributor.value());

            if (sum.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("Invalid withdrawal plan: sum > 1");
            }

            //Percentage kratos = Percentage.of(BigDecimal.ONE.subtract(sum));

            // 5. Accounts
            String agentAccountId = ctx.getAccountFor("AGENT");

            // 6. Lines
            if (!agentWithdrawal.value().equals(BigDecimal.ZERO)) {
                lines.add(line(BeneficiaryType.AGENT, agentAccountId, agentWithdrawal, base));
            }

            return lines;
        }

        throw new IllegalArgumentException("Unsupported commission strategy: " + strategy.getClass().getSimpleName());
    }

    private List<CommissionDistributionResult.Line> distributeShares(
            List<CommissionShare> shares,
            PricingRequestContext ctx,
            Money base,
            boolean rejectKratos
    ) {
        if (shares == null || shares.isEmpty()) throw new IllegalArgumentException("shares is required");

        BigDecimal sum = shares.stream()
                .map(s -> s.share().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Invalid commission shares: sum > 1.0");
        }

        List<CommissionDistributionResult.Line> lines = new ArrayList<>();

        for (CommissionShare s : shares) {
            String beneficiary = s.beneficiaryType().name(); // ex: "AGENT", "DISTRIBUTOR"
            // On récupère dynamiquement l'ID du compte depuis la hiérarchie du contexte
            String targetAccountId = ctx.getAccountFor(beneficiary);

            if (rejectKratos && s.beneficiaryType() == BeneficiaryType.KRATOS) {
                throw new IllegalArgumentException("DEPOSIT plan must not include KRATOS as beneficiary");
            }

            lines.add(line(s.beneficiaryType(), targetAccountId, s.share(), base));
        }

        return lines;
    }

    private CommissionDistributionResult.Line line(BeneficiaryType b, String accountId, Percentage p, Money base) {
        Money amount = multiply(base, p);
        return new CommissionDistributionResult.Line(b.name(), accountId, p.value().toPlainString(), amount);
    }

    private Money multiply(Money base, Percentage p) {
        var v = base.amount().multiply(p.value());
        return new Money(v, base.currency());
    }

    private void ensure(boolean ok, String msg) {
        if (!ok) throw new IllegalArgumentException(msg);
    }

    private Percentage findShare(List<CommissionShare> shares, BeneficiaryType type) {
        return shares.stream()
                .filter(s -> s.beneficiaryType() == type)
                .map(CommissionShare::share)
                .findFirst()
                .orElse(Percentage.ZERO);
    }
}