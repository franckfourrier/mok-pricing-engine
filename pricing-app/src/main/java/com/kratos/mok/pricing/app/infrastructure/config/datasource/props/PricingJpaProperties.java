package com.kratos.mok.pricing.app.infrastructure.config.datasource.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pricing.jpa")
public class PricingJpaProperties {

    private final Hibernate hibernate = new Hibernate();

    public Hibernate getHibernate() {
        return hibernate;
    }

    public static class Hibernate {

        private String ddlAuto;

        public String getDdlAuto() {
            return ddlAuto;
        }

        public void setDdlAuto(String ddlAuto) {
            this.ddlAuto = ddlAuto;
        }
    }
}