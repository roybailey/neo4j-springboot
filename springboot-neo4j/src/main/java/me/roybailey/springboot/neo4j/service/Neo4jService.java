package me.roybailey.springboot.neo4j.service;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;


@Slf4j
@Service
@Getter
public class Neo4jService {

    @Value("${neo4j.driver}")
    String neo4jDriver;

    @Value("${neo4j.uri}")
    String neo4jUri;

    @Autowired
    SessionFactory neo4jSessionFactory;


    public boolean isEmbedded() {
        return (neo4jDriver.indexOf("embedded") >= 0);
    }


    public void registerProcedures(List<Class<?>> toRegister) {
        if(isEmbedded()) {
            EmbeddedDriver embeddedDriver = (EmbeddedDriver) neo4jSessionFactory.getDriver();
            GraphDatabaseService databaseService = embeddedDriver.getGraphDatabaseService();
            Procedures procedures = ((GraphDatabaseAPI) databaseService).getDependencyResolver().resolveDependency(Procedures.class);
            toRegister.forEach((proc) -> {
                try {
                    procedures.registerProcedure(proc);
                } catch (KernelException e) {
                    throw new RuntimeException("Error registering " + proc, e);
                }
            });
        }
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
