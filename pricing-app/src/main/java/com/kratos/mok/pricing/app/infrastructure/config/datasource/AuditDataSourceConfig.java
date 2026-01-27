package com.kratos.mok.pricing.app.infrastructure.config.datasource;

import com.kratos.mok.pricing.app.infrastructure.config.datasource.props.AuditJpaProperties;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(AuditJpaProperties.class)
@EnableJpaRepositories(
        basePackages = "com.kratos.mok.pricing.audit.infrastructure.repository",
        entityManagerFactoryRef = "auditEntityManagerFactory",
        transactionManagerRef = "auditTransactionManager"
)
public class AuditDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.audit")
    public DataSourceProperties auditDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "auditDataSource")
    public DataSource auditDataSource(
            @Qualifier("auditDataSourceProperties") DataSourceProperties props
    ) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "auditEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("auditDataSource") DataSource dataSource,
            AuditJpaProperties auditJpaProperties
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.kratos.mok.pricing.audit")
                .persistenceUnit("audit")
                .properties(auditHibernateProperties(auditJpaProperties))
                .build();
    }

    @Bean(name = "auditTransactionManager")
    public PlatformTransactionManager auditTransactionManager(
            @Qualifier("auditEntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }

    private Map<String, Object> auditHibernateProperties(AuditJpaProperties props) {
        Map<String, Object> map = new LinkedHashMap<>();
        // ✅ pas en dur : ça dépend du profil via YAML
        map.put("hibernate.hbm2ddl.auto", props.getHibernate().getDdlAuto());
        return map;
    }
}