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
        ensure(props.getCantonnement(), "Compte Cantonnement");
        ensure(props.getExploitation(), "Compte Exploitation");
        ensure(props.getTaxRate(), "Sous-compte taxe (Rate)");
        ensure(props.getTaxFixed(), "Sous-compte taxe (Fixed)");
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
