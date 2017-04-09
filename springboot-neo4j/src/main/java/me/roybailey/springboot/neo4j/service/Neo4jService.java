package me.roybailey.springboot.neo4j.service;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;

@Service
public class Neo4jService {

    @Value("${neo4j.driver}")
    String neo4jDriver;

    @Autowired
    SessionFactory neo4jSessionFactory;


    public Neo4jTemplate getNeo4jTemplate() {
        return new Neo4jTemplate(neo4jSessionFactory.openSession());
    }


    public boolean isEmbedded() {
        return (neo4jDriver.indexOf("embedded") >= 0);
    }


    public String loadCypher(String filename) {
        URL url = Resources.getResource(filename);
        String cypher = null;
        try {
            cypher = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException err) {
            err.printStackTrace();
        }
        return cypher;
    }


    public void runCypher(String cypher) {
        Session session = neo4jSessionFactory.openSession();
        session.query(cypher, Collections.emptyMap());
    }

}
