package me.roybailey.springboot.neo4j.repository;

import apoc.coll.Coll;
import apoc.convert.Json;
import apoc.create.Create;
import apoc.help.Help;
import apoc.index.FulltextIndex;
import apoc.load.LoadJson;
import apoc.load.Xml;
import apoc.meta.Meta;
import apoc.path.PathExplorer;
import apoc.refactor.GraphRefactoring;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.neo4j.service.Neo4jService;
import org.assertj.core.api.JUnitBDDSoftAssertions;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class Neo4jProcedureTest {


    @Autowired
    Neo4jService neo4jService;

    @Autowired
    SessionFactory sessionFactory;


    @Rule
    public TestName name = new TestName();

    @Rule
    public final JUnitBDDSoftAssertions softly = new JUnitBDDSoftAssertions();


    /**
     * Test simple cypher query as sanity check before testing procedures
     */
    @Test
    public void testNeo4jQuery() {

        Session session = sessionFactory.openSession();
        Result result = session.query("MATCH (n) RETURN n LIMIT 10", ImmutableMap.of());

        assertThat(result).isNotNull();
        List<Map<String, Object>> dataList = StreamSupport.stream(result.spliterator(), false)
                .collect(Collectors.toList());
        log.info("data\n{}", dataList);
        assertThat(dataList).isNotNull();
        assertThat(dataList.size()).isGreaterThan(0);
    }


    /**
     * Test we can call out to standard built-in procedures using cypher
     */
    @Test
    public void testNeo4jProcedureCalls() {

        Session session = sessionFactory.openSession();
        Result result = session.query("CALL dbms.procedures()", ImmutableMap.of());

        assertThat(result).isNotNull();
        List<Map<String, Object>> dataList = StreamSupport.stream(result.spliterator(), false)
                .collect(Collectors.toList());
        log.info("data\n{}", dataList);
        assertThat(dataList).isNotNull();
        assertThat(dataList.size()).isGreaterThan(0);
    }


    /**
     * Test we can call out to https://neo4j-contrib.github.io/neo4j-apoc-procedures
     * to show the plugin is installed correctly
     */
    @Test
    public void testNeo4jApocProcedureCalls() {

        neo4jService.registerProcedures(asList(
                Help.class,
                Coll.class,
                apoc.map.Maps.class,
                Json.class,
                Create.class,
                apoc.date.Date.class,
                FulltextIndex.class,
                apoc.lock.Lock.class,
                LoadJson.class,
                Xml.class,
                PathExplorer.class,
                Meta.class,
                GraphRefactoring.class)
        );
        Session session = sessionFactory.openSession();
        Result result = session.query("CALL apoc.help('apoc')", ImmutableMap.of());

        assertThat(result).isNotNull();
        List<Map<String, Object>> dataList = StreamSupport.stream(result.spliterator(), false)
                .collect(Collectors.toList());
        log.info("data\n{}", dataList);
        assertThat(dataList).isNotNull();
        assertThat(dataList.size()).isGreaterThan(0);
    }

}
