@ApplicationModule(
        displayName = "Regulatory Control",
        allowedDependencies = {
                "pricing-fees",
                "pricing-shared"
        }
)
package com.kratos.mok.pricing.control;

import org.springframework.modulith.ApplicationModule;