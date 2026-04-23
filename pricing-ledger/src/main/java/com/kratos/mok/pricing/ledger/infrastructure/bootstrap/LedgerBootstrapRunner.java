package com.kratos.mok.pricing.ledger.infrastructure.bootstrap;

import com.kratos.mok.pricing.ledger.infrastructure.model.LedgerAccountEntity;
import com.kratos.mok.pricing.ledger.infrastructure.repository.JpaLedgerAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(LedgerAccountsProperties.class)
@Profile({"dev","docker"})
public class LedgerBootstrapRunner implements ApplicationRunner {

    private final JpaLedgerAccountRepository jpa;
    private final LedgerAccountsProperties props;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensure(props.getCantonment(), "Compte Cantonnement");
        ensure(props.getExploitation(), "Compte Exploitation");
        ensure(props.getTax(), "Compte Taxe");
        ensure(props.getTaxRate(), "Sous-compte Taxe (Rate)");
        ensure(props.getTaxFixed(), "Sous-compte Taxe (Fixed)");
        ensure(props.getDistributed(), "Compte Solde Distribué");
        ensure(props.getDistributedSuperDistributor(), "Sous-compte Solde Distribué - Super Distributeur");
        ensure(props.getDistributedDistributor(), "Sous-compte Solde Distribué - Distributeur");
        ensure(props.getDistributedAgent(), "Sous-compte Solde Distribué - Agent");
        ensure(props.getExternal(), "Compte Reversement Ext.");
        ensure(props.getBankClearing(), "Compte de Compensation bancaire");
    }

    private void ensure(String code, String name) {
        String c = code.trim().toUpperCase();
        if (jpa.existsByCode(c)) return;

        LedgerAccountEntity a = new LedgerAccountEntity();
        a.setCode(c);
        a.setName(name);
        a.setCurrency(props.getCurrency().toUpperCase());
        a.setCreatedAt(LocalDateTime.now());
        jpa.save(a);
    }
}
