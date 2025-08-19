package check.demo.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "check.demo.repository.metrics",
        entityManagerFactoryRef = "metricsEmf",
        transactionManagerRef = "metricsTx"
)
public class MetricsDbConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.metrics")
    public DataSource metricsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "metricsEmf")
    public LocalContainerEntityManagerFactoryBean metricsEmf(EntityManagerFactoryBuilder builder,
                                                             @Qualifier("metricsDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("check.demo.model.metrics")
                .persistenceUnit("metrics")
                .properties(Map.of(
                        "hibernate.hbm2ddl.auto", "validate",
                        "hibernate.dialect", "org.hibernate.dialect.MariaDBDialect",
                        "hibernate.show_sql", "true"
                ))
                .build();
    }

    @Bean(name = "metricsTx")
    public PlatformTransactionManager metricsTx(@Qualifier("metricsEmf") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
