package com.kratos.mok.pricing.taxes.infrastructure.config;

import com.kratos.mok.pricing.taxes.domain.service.TaxPolicyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaxesDomainConfig {
    @Bean
    public TaxPolicyResolver taxPolicyResolver() {
        return new TaxPolicyResolver();
    }
}
