package com.kratos.mok.pricing.app.architecture;

import com.kratos.mok.pricing.app.MoKPricingEngineApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArchitectureTest {

    @Test
    void verifiesModularStructure() {
        ApplicationModules.of(MoKPricingEngineApplication.class)
                .verify();
    }
}
