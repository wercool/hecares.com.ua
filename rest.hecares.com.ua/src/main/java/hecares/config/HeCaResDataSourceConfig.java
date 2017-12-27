package hecares.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "hecaresEMF", 
        basePackages = { "hecares.repository" },
        transactionManagerRef = "hecaresTM")
public class HeCaResDataSourceConfig {

    @Value("${hecares.hibernate.dialect}")
    private String HIBERNATE_DIALECT;

    @Value("${hecares.hibernate.naming-strategy}")
    private String HIBERNATE_NAMING_STRATEGY;

//    @Value("${hecares.hibernate.ddl-auto}")
//    private String HIBERNATE_DDL_AUTO;

    @Value("${hecares.hibernate.hbm2ddl.auto}")
    private String HIBERNATE_HBM2DDL_AUTO;

    @Value("${hecares.hibernate.show_sql}")
    private String HIBERNATE_SHOW_SQL;

     // HeCaRes Data Source (primary)
    @Primary
    @Bean(name = "hecaresDS")
    @ConfigurationProperties(prefix="hecares.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    // HeCaRes Entity Manager Factory (primary)
    @Primary
    @Bean(name = "hecaresEMF")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("hecaresDS") DataSource dataSource) {
        

        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.dialect", HIBERNATE_DIALECT);
        jpaProperties.setProperty("hibernate.naming-strategy", HIBERNATE_NAMING_STRATEGY);
//        jpaProperties.setProperty("hibernate.ddl-auto", HIBERNATE_DDL_AUTO);
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", HIBERNATE_HBM2DDL_AUTO);
        jpaProperties.setProperty("hibernate.show_sql", HIBERNATE_SHOW_SQL);

        LocalContainerEntityManagerFactoryBean emf = builder
                                                     .dataSource(dataSource)
                                                     .packages("hecares.model")
                                                     .persistenceUnit("hecares")
                                                     .build();
        emf.setJpaProperties(jpaProperties);
        return emf;
    }


    // HeCaRes Transaction Manager (primary)
    @Primary
    @Bean(name = "hecaresTM")
    public PlatformTransactionManager transactionManager(
            @Qualifier("hecaresEMF") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
