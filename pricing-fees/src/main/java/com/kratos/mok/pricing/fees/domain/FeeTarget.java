package com.kratos.mok.pricing.fees.domain;

import java.util.Objects;

public record FeeTarget(Scope scope, String value) {

    public enum Scope {
        GLOBAL(0),      // Priorité basse (ex: Tout le monde)
        PROFILE(1),     // Priorité moyenne (ex: PREMIUM, AGENT)
        INDIVIDUAL(2);  // Priorité haute (ex: Compte spécifique)

        private final int priority;
        Scope(int priority) { this.priority = priority; }
        public int priority() { return priority; }
    }

    // Factory: Cible Globale (Pour tout le monde)
    public static FeeTarget global() {
        return new FeeTarget(Scope.GLOBAL, "ALL");
    }

    // Factory: Cible par Catégorie/Profil
    public static FeeTarget profile(String profileName) {
        Objects.requireNonNull(profileName);
        return new FeeTarget(Scope.PROFILE, profileName);
    }

    // Factory: Cible Individuelle
    public static FeeTarget individual(String accountId) {
        Objects.requireNonNull(accountId);
        return new FeeTarget(Scope.INDIVIDUAL, accountId);
    }
}
