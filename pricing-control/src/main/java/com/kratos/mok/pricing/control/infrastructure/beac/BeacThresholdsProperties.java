package com.kratos.mok.pricing.control.infrastructure.beac;

import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "control.beac")
public record BeacThresholdsProperties(

        @NotBlank String currency,

        @NotNull @Valid ValueLimits valueLimits,

        @NotNull Map<TransactionType, Integer> volumeMonthly
) {

    public record ValueLimits(
            @NotNull String instrumentCeiling,
            @NotNull String cashLoadingDaily,
            @NotNull String bankLoadingDaily,

            @NotNull Map<TransactionType, String> perOperation,
            @NotNull Map<TransactionType, String> daily,

            @NotNull Combined combined
    ) {
        public Money instrumentCeilingMoney(String currency) { return Money.of(instrumentCeiling, currency); }
        public Money cashLoadingDailyMoney(String currency) { return Money.of(cashLoadingDaily, currency); }
        public Money bankLoadingDailyMoney(String currency) { return Money.of(bankLoadingDaily, currency); }

        public Money perOperationMoney(TransactionType type, String currency) {
            String v = perOperation.get(type);
            return v == null ? null : Money.of(v, currency);
        }

        public Money dailyMoney(TransactionType type, String currency) {
            String v = daily.get(type);
            return v == null ? null : Money.of(v, currency);
        }
    }

    public record Combined(
            @NotNull String daily,
            @NotNull String weekly,
            @NotNull String monthly
    ) {
        public Money dailyMoney(String currency) { return Money.of(daily, currency); }
        public Money weeklyMoney(String currency) { return Money.of(weekly, currency); }
        public Money monthlyMoney(String currency) { return Money.of(monthly, currency); }
    }

    public String normalizedCurrency() {
        return currency.trim().toUpperCase();
    }
}
