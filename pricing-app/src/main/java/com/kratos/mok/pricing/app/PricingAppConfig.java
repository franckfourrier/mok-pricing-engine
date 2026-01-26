package com.kratos.mok.pricing.app;

import com.kratos.mok.pricing.app.bootstrap.FeeBootstrapProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FeeBootstrapProperties.class)
public class PricingAppConfig {}
