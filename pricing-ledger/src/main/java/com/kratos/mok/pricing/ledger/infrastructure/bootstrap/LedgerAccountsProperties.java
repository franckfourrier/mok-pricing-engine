package com.kratos.mok.pricing.ledger.infrastructure.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ledger.accounts")
public class LedgerAccountsProperties {
    private String cantonnement = "ACC-CANT";
    private String exploitation = "ACC-EXP";
    private String taxRate = "ACC-TAX-RATE";
    private String taxFixed = "ACC-TAX-FIXED";

    private String currency = "XAF";
}
