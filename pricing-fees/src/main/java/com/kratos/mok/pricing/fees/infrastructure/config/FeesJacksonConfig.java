package com.kratos.mok.pricing.fees.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kratos.mok.pricing.fees.domain.vo.FeePercentage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeesJacksonConfig {

    private final ObjectMapper globalObjectMapper;

    @PostConstruct
    public void registerModuleMixins() {
        globalObjectMapper.addMixIn(FeePercentage.class, FeePercentageMixin.class);
    }
}