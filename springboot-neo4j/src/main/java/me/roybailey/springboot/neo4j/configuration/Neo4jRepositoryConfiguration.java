package me.roybailey.springboot.neo4j.configuration;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;
import java.io.IOException;

@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
//@ComponentScan("me.roybailey.springboot.neo4j")
@EnableNeo4jRepositories("me.roybailey.springboot.neo4j.repository")
public class Neo4jRepositoryConfiguration extends org.springframework.data.neo4j.config.Neo4jConfiguration {

    @Value("${neo4j.driver}")
    String neo4jDriver;

    @Value("${neo4j.uri}")
    String neo4jURI;

    @Override
    public SessionFactory getSessionFactory() {
        return new SessionFactory(
                neo4jConfiguration(),
                "me.roybailey.springboot.neo4j.domain");
    }

    @Bean
    public org.neo4j.ogm.config.Configuration neo4jConfiguration() {
        log.info("neo4j.driver=" + neo4jDriver);
        // convert relative embedded database path into URI
        if (neo4jDriver.indexOf("embedded") > 0 && !neo4jURI.startsWith("file://")) {
            //neo4jURI = "file://" + new File(neo4jURI).getAbsolutePath();
            try {
                // trouble with Windows URIs not working due to drive colon, taking them out seems to fix it
                neo4jURI = (new File(neo4jURI).getCanonicalFile().toURI().toString()).replaceAll("file:\\/[\\w]:","file://");
            } catch (IOException err) {
                log.error("Error converting relative file path into absolute URI for embedded driver",err);
                log.error("Try updating properties to use absolute URI path for embedded driver instead");
                System.exit(-1);
            }
        }
        log.info("neo4j.uri=" + neo4jURI);
        org.neo4j.ogm.config.Configuration configuration = new org.neo4j.ogm.config.Configuration();
        configuration.driverConfiguration()
                .setDriverClassName(neo4jDriver)
                .setURI(neo4jURI);
        return configuration;
    }

}


