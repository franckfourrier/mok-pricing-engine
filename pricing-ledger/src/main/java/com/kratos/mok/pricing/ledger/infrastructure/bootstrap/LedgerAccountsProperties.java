package com.kratos.mok.pricing.ledger.infrastructure.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ledger.accounts")
public class LedgerAccountsProperties {
    private String cantonment = "ACC-CANT";
    private String exploitation = "ACC-EXP";
    private String taxRate = "ACC-TAX-RATE";
    private String taxFixed = "ACC-TAX-FIXED";
    private String tax = "ACC-TAX";
    private String bankClearing = "ACC-BANK-CLEAR";
    private String distributed = "ACC-DIST";
    private String distributedSuperDistributor = "ACC-DIST-SUPER-DISTRIBUTOR";
    private String distributedDistributor = "ACC-DIST-DISTRIBUTOR";
    private String distributedAgent = "ACC-DIST-AGENT";
    private String external = "ACC-EXT";
    private String currency = "XAF";
}