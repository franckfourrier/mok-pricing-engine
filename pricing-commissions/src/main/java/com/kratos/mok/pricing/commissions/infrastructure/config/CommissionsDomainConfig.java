package com.kratos.mok.pricing.commissions.infrastructure.config;

import com.kratos.mok.pricing.commissions.domain.service.CommissionPlanResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommissionsDomainConfig {

    @Bean
    public CommissionPlanResolver commissionPlanResolver() {
        return new CommissionPlanResolver();
    }
}
