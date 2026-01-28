package com.kratos.mok.pricing.app.bootstrap;

import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.fees.domain.enums.KycRequirement;
import com.kratos.mok.pricing.fees.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "bootstrap")
public record FeeBootstrapProperties(
        @NotNull Integer version,
        @NotEmpty @Valid List<FeePolicyYaml> fees
) {
    public record FeePolicyYaml(
            @NotNull TransactionType transactionType,

            @NotNull @Valid TargetYaml target,

            @NotNull FeeStrategyType strategyType,

            // FIXED
            String fixedAmount,

            // PROPORTIONAL
            String percentage,

            // TIERED
            @Valid List<TierYaml> tiers,

            @NotNull String activationThreshold,
            String minFee,
            String maxFee,

            @Min(0) Integer minMonthlyTxCount,

            @NotNull KycRequirement kycRequirement
    ) {}

    public record TargetYaml(
            @NotNull TargetScope scope,
            @NotNull String value
    ) {}

    public record TierYaml(
            @NotNull String min,
            @NotNull String max,
            @NotNull FeeStrategyType tierStrategyType,
            @NotNull String tierValue
    ) {}
}
