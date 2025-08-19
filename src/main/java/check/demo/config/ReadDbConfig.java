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
        basePackages = "check.demo.repository.read",
        entityManagerFactoryRef = "readEmf",
        transactionManagerRef = "readTx"
)
public class ReadDbConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.read")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "readEmf")
    public LocalContainerEntityManagerFactoryBean readEmf(
            EntityManagerFactoryBuilder builder,
            @Qualifier("readDataSource") DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("check.demo.model.read")  // Cctv 엔티티 패키지
                .persistenceUnit("read")
                .properties(Map.of(
                        "hibernate.hbm2ddl.auto", "validate",
                        "hibernate.dialect", "org.hibernate.dialect.MariaDBDialect",
                        "hibernate.show_sql", "false"
                ))
                .build();
    }

    @Bean(name = "readTx")
    public PlatformTransactionManager readTx(@Qualifier("readEmf") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
