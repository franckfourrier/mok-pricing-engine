package com.kratos.mok.pricing.app.infrastructure.config.datasource;

import com.kratos.mok.pricing.app.infrastructure.config.datasource.props.PricingJpaProperties;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(PricingJpaProperties.class)
@EnableJpaRepositories(
        basePackages = {
                "com.kratos.mok.pricing.fees.infrastructure.repository",
                "com.kratos.mok.pricing.taxes.infrastructure.repository",
                "com.kratos.mok.pricing.commissions.infrastructure.repository",
                "com.kratos.mok.pricing.ledger.infrastructure.repository",
                "com.kratos.mok.pricing.app.infrastructure.repository"

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

    @Bean(name = "pricingDataSource")
    @Primary
    public DataSource pricingDataSource(
            @Qualifier("pricingDataSourceProperties") DataSourceProperties props
    ) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "pricingEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean pricingEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("pricingDataSource") DataSource dataSource,
            PricingJpaProperties pricingJpaProperties
    ) {
        return builder
                .dataSource(dataSource)
                .packages(
                        "com.kratos.mok.pricing.fees",
                        "com.kratos.mok.pricing.taxes",
                        "com.kratos.mok.pricing.commissions",
                        "com.kratos.mok.pricing.ledger",
                        "com.kratos.mok.pricing.app"
                )
                .persistenceUnit("pricing")
                .properties(pricingHibernateProperties(pricingJpaProperties))
                .build();
    }

    @Bean(name = "pricingTransactionManager")
    @Primary
    public PlatformTransactionManager pricingTransactionManager(
            @Qualifier("pricingEntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }

    private Map<String, Object> pricingHibernateProperties(PricingJpaProperties props) {
        Map<String, Object> map = new LinkedHashMap<>();
        // ✅ pas en dur : ça dépend du profil via YAML
        map.put("hibernate.hbm2ddl.auto", props.getHibernate().getDdlAuto());
        return map;
    }
}