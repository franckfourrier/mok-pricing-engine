package com.kratos.mok.pricing.control.infrastructure.config;

import com.kratos.mok.pricing.control.infrastructure.beac.BeacThresholdsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BeacThresholdsProperties.class)
public class BeacControlConfig {
}