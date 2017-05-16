package me.roybailey.springboot.repository;

import apoc.convert.Json;
import apoc.load.Jdbc;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.jpa.repository.ProductRepository;
import me.roybailey.springboot.neo4j.repository.MovieRepository;
import me.roybailey.springboot.neo4j.repository.PersonRepository;
import me.roybailey.springboot.neo4j.service.Neo4jService;
import org.assertj.core.api.JUnitBDDSoftAssertions;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApocJdbcImportTest {

    @Autowired
    private Neo4jService neo4jService;

    @Rule
    public TestName name = new TestName();

    @Rule
    public final JUnitBDDSoftAssertions softly = new JUnitBDDSoftAssertions();

    @Value("${spring.datasource.url};USER=${spring.datasource.username}")
    private String JDBC;


    /**
     * Test we can call out to https://neo4j-contrib.github.io/neo4j-apoc-procedures
     * to show the ability to use apoc's jdbc loading cypher to load our JPA data
     */
    @Test
    public void testNeo4jApocProcedureCalls() {

        neo4jService.registerProcedures(asList(Json.class, Jdbc.class));
        Session session = neo4jService.getNeo4jSessionFactory().openSession();
        Result result = session.query("CALL apoc.load.jdbc('" + JDBC + "','PRODUCT')", ImmutableMap.of());

        assertThat(result).isNotNull();
        List<Map<String, Object>> dataList = StreamSupport.stream(result.spliterator(), false)
                .collect(Collectors.toList());
        dataList.stream().map(Object::toString).forEach(log::info);
        assertThat(dataList).isNotNull();
        assertThat(dataList.size()).isEqualTo(3);
    }
}

