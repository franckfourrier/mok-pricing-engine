package com.kratos.mok.pricing.fees.infrastructure.config;

import com.kratos.mok.pricing.fees.domain.service.FeePolicyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeesDomainConfig {

    @Bean
    public FeePolicyResolver feePolicyResolver(){
        return new FeePolicyResolver();
    }
}
