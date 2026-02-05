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
        "com.kratos.mok.pricing.taxes",
        "com.kratos.mok.pricing.commissions",
        "com.kratos.mok.pricing.control",
        "com.kratos.mok.pricing.audit",
        "com.kratos.mok.pricing.ledger",
        "com.kratos.mok.pricing.shared"
})
public class MoKPricingEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoKPricingEngineApplication.class, args);
    }

}
