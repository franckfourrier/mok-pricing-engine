module com.kratos.mok.pricing.fees {
    requires com.kratos.mok.pricing.shared;

    // 🔓 Ports & contrats exposés
    exports com.kratos.mok.pricing.fees.domain.gateway;
    exports com.kratos.mok.pricing.fees.domain.compliance;
    exports com.kratos.mok.pricing.fees.domain.strategy;
}
