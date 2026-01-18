package com.kratos.mok.pricing.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {
        "com.kratos.mok.pricing.app",
        "com.kratos.mok.pricing.fees",
        "com.kratos.mok.pricing.control",
        "com.kratos.mok.pricing.audit"
})
public class MoKPricingEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoKPricingEngineApplication.class, args);
    }

}
