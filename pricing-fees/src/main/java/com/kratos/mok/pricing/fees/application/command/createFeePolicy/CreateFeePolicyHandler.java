package com.kratos.mok.pricing.fees.application.command.createFeePolicy;

import com.kratos.mok.pricing.fees.domain.compliance.FeePolicyComplianceData;
import com.kratos.mok.pricing.fees.domain.gateway.RegulatoryGatekeeper;
import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateFeePolicyHandler {

    private final FeePolicyRepository repository;
    private final RegulatoryGatekeeper regulatoryGatekeeper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateFeePolicyResponse handle(CreateFeePolicyCommand cmd) {
        log.info("Traitement commande création politique");

        // 1. Transformation Command -> Domaine
        // Ici, la Barrière 1 (Taux positifs, min<max) est déclenchée automatiquement
        // lors de la création des objets (Money, FeePercentage, Strategy) inclus dans la commande.
        FeePolicy policy = FeePolicy.create(
                cmd.type(), cmd.target(), cmd.strategy(),
                cmd.limits(), cmd.activationThreshold(), cmd.validity(),
                cmd.kycRequired(), cmd.authorId()
        );

        // 2. Domaine : Vérification de chevauchement (Barrier 2)
        /*if (repository.existsConflictingPolicy(policy)) {
            throw new IllegalArgumentException("Une politique active existe déjà pour ce périmètre.");
        }*/

        // 3. Contrôle : Vérification BEAC (Barrier 3)
        /*FeePolicyComplianceData complianceData = policy.toComplianceData();
        try {
            regulatoryGatekeeper.validate(complianceData); // Appel Synchrone
        } catch (RegulatoryViolationException e) {
            // "Le process bifurque immédiatement vers Blocage automatique + Alerte"
            publishAudit(policy, "BLOCAGE_BEAC", command.authorId(), e.getMessage());
            throw e; // Arrêt du traitement (La transaction n'est pas sauvée)
        }*/

        // 4. Persistance (Write Model)
        repository.save(policy);

        // 5. Audit
        /*publishAudit(policy, "CREATION_SUCCES", command.authorId(), "Configuration validée");

        return policy.getId();*/
        return new CreateFeePolicyResponse("1", true);
    }

    private void publishAudit(FeePolicy policy, String action, String actor, String reason) {
        // ... (Même logique d'event que précédemment) ...
    }

       /* repository.save(policy);
        return new CreateFeePolicyResponse(policy.snapshot().id(), true);*/
}