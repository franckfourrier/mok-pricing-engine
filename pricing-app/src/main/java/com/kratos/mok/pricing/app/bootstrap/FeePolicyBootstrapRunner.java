package com.kratos.mok.pricing.app.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile({"dev", "local", "bootstrap"})
public class FeePolicyBootstrapRunner implements ApplicationRunner {

    private final FeeBootstrapProperties props;
    private final FeePolicyBootstrapService bootstrapService;

    @Override
    public void run(ApplicationArguments args) {
        bootstrapService.bootstrap(props);
    }
}
