package com.kratos.mok.pricing.shared.domain.money;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormatter {

    private MoneyFormatter() {}

    /**
     * Transforme XAF en FCFA, sinon garde le code ISO.
     */
    public static String getCurrencyLabel(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) return "";
        return "XAF".equalsIgnoreCase(isoCode.trim()) ? "FCFA" : isoCode.toUpperCase();
    }

    /**
     * Formate le montant avec séparateurs de milliers et 2 décimales.
     * Exemple: 182.5 -> "182,50 FCFA"
     */
    public static String format(BigDecimal amount, String isoCode) {
        if (amount == null) return "";

        // Utilisation d'un DecimalFormat pour garantir le formatage français (espaces/virgules)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' '); // Espace pour les milliers
        symbols.setDecimalSeparator(',');  // Virgule pour les centimes

        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);

        return String.format("%s %s", df.format(amount), getCurrencyLabel(isoCode));
    }
}