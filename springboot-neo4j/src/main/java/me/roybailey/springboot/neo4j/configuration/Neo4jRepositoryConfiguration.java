package me.roybailey.springboot.neo4j.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableNeo4jRepositories("me.roybailey.springboot.neo4j.repository")
public class Neo4jRepositoryConfiguration {

    @Value("${neo4j.driver}")
    String neo4jDriver;

    @Value("${neo4j.uri}")
    String neo4jURI;

    /**
     * Create the Neo4j configuration here so we can use spring properties instead of ogm.properties
     */
    @Bean
    public org.neo4j.ogm.config.Configuration neo4jConfiguration() {
        log.info("neo4j.driver=" + neo4jDriver);
        log.info("neo4j.uri=" + neo4jURI);
        org.neo4j.ogm.config.Configuration configuration = new org.neo4j.ogm.config.Configuration();
        configuration.driverConfiguration().setDriverClassName(neo4jDriver);
        // only set the URI if it has a value, as not setting it for embedded is needed to create impermanent database
        if (!StringUtils.isEmpty(neo4jURI))
            configuration.driverConfiguration().setURI(neo4jURI);
        return configuration;
    }

    @Bean
    public SessionFactory getSessionFactory() {
        return new SessionFactory(
                neo4jConfiguration(),
                "me.roybailey.springboot.neo4j.domain");
    }

    @Bean
    public Neo4jTransactionManager transactionManager() {
        return new Neo4jTransactionManager(getSessionFactory());
    }

}


