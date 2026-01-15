package com.kratos.mok.pricing.app.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.kratos.mok.pricing.fees",
                "com.kratos.mok.pricing.taxes",
                "com.kratos.mok.pricing.commissions"
        },
        entityManagerFactoryRef = "pricingEntityManagerFactory",
        transactionManagerRef = "pricingTransactionManager"
)
public class PricingDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.pricing")
    public DataSourceProperties pricingDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource pricingDataSource() {
        return pricingDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean pricingEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(pricingDataSource())
                .packages(
                        "com.kratos.mok.pricing.fees",
                        "com.kratos.mok.pricing.taxes",
                        "com.kratos.mok.pricing.commissions"
                )
                .persistenceUnit("pricing")
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager pricingTransactionManager(
            @Qualifier("pricingEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
