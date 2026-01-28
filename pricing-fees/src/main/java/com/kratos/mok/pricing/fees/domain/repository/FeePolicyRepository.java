package com.kratos.mok.pricing.fees.domain.repository;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;
import com.kratos.mok.pricing.fees.domain.vo.FeePolicyId;

import java.util.List;
import java.util.Optional;

public interface FeePolicyRepository {

    void save(FeePolicy policy);

    Optional<FeePolicy> findById(FeePolicyId id);

    /**
     * Candidats pour (type, accountType, accountId):
     * - GLOBAL/ALL
     * - ACCOUNT_TYPE/accountType
     * - ACCOUNT_ID/accountId
     */
    List<FeePolicy> findCandidates(TransactionType type, String accountType, String accountId);

    /**
     * Barrière 2 : détecter chevauchement (même transactionType + même scope/value + fenêtre qui se recouvre)
     * sur des policies "actives" (ACTIVE, PENDING_APPROVAL, SUSPENDED...) selon ta règle métier.
     */
    boolean existsConflictingPolicy(FeePolicy policy);

    /**
     * Pour bootstrap: vérifier si une policy existe déjà pour (transactionType + scope + value),
     * quel que soit le statut (ou seulement ACTIVE selon ton choix, mais sois cohérent).
     */
    boolean existsAnyFor(TransactionType transactionType, TargetScope scope, String value);
}
