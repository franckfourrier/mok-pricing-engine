package com.kratos.mok.pricing.shared.domain.money;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormatter {

    private MoneyFormatter() {}

    public static String getCurrencyLabel(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) return "";
        return "XAF".equalsIgnoreCase(isoCode.trim()) ? "Fcfa" : isoCode.toUpperCase();
    }

    public static String format(BigDecimal amount, String isoCode) {
        return format(amount, isoCode, "#,##0.00");
    }

    public static String formatUi(BigDecimal amount, String isoCode) {
        return format(amount, isoCode, "#,##0.##");
    }

    private static String format(BigDecimal amount, String isoCode, String pattern) {
        if (amount == null) return "";

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');

        DecimalFormat df = new DecimalFormat(pattern, symbols);

        return df.format(amount) + " " + getCurrencyLabel(isoCode);
    }
}