package me.roybailey.springboot.neo4j.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
//@EnableJpaRepositories(value = "me.roybailey.springboot.jpa.repository")
//@EnableNeo4jRepositories(value = "me.roybailey.springboot.neo4j.repository")
public class ChainedNeo4jSpringConfiguration extends Neo4jRepositoryConfiguration {

    @Autowired
    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager neo4jTransactionManager() throws Exception {
        log.info("Creating ChainedTransactionManager with JPA+NEO4J");
        return new ChainedTransactionManager(
                new JpaTransactionManager(entityManagerFactoryBean.getObject()),
                super.transactionManager()
        );
    }
}
