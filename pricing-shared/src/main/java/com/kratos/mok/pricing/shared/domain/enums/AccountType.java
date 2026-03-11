package com.kratos.mok.pricing.shared.domain.enums;

public enum AccountType {
    STANDARD,
    PREMIUM,
    SUBSCRIBER,
    AGENT,
    DISTRIBUTOR,
    SUPER_DISTRIBUTOR,
    MERCHANT,
    SUPPLIER,
    SPECIFIC_USER;

    public String label() {
        return switch (this) {
            case STANDARD -> "Standard";
            case PREMIUM -> "Premium";
            case SUBSCRIBER -> "Abonné";
            case AGENT -> "Agent";
            case DISTRIBUTOR -> "Distributeur";
            case SUPER_DISTRIBUTOR -> "Super distributeur";
            case MERCHANT -> "Marchand";
            case SUPPLIER -> "Fournisseur";
            case SPECIFIC_USER -> "Utilisateur spécifique";
        };
    }
}
