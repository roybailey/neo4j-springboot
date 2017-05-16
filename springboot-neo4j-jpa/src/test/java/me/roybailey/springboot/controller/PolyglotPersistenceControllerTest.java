package me.roybailey.springboot.controller;

import com.google.common.collect.ImmutableMap;
import feign.*;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.jpa.domain.Product;
import me.roybailey.springboot.neo4j.domain.Movie;
import me.roybailey.springboot.neo4j.service.Neo4jService;
import org.assertj.core.api.JUnitBDDSoftAssertions;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PolyglotPersistenceControllerTest {

    @LocalServerPort
    int port;

    @Rule
    public TestName name = new TestName();

    @Rule
    public final JUnitBDDSoftAssertions softly = new JUnitBDDSoftAssertions();


    @Headers("Accept: application/json")
    public interface Neo4jMovieApi {

        @RequestLine("GET /movie")
        List<Movie> getAllMovies();

        @RequestLine("GET /movie/{id}")
        Movie getMovieById(@Param("id") Long id);

        @Headers("Content-Type: application/json")
        @RequestLine("POST /movie")
        Movie saveMovie(Movie movie);

        @RequestLine("DELETE /movie/{id}")
        Response deleteMovie(@Param("id") Long id);
    }


    @Headers("Accept: application/json")
    public interface JpaProductApi {

        @RequestLine("GET /product")
        List<Product> getAllProducts();

        @RequestLine("GET /product/{id}")
        Product getProductById(@Param("id") Long id);

        @Headers("Content-Type: application/json")
        @RequestLine("POST /product")
        Product saveProduct(Product product);

        @RequestLine("DELETE /product/{id}")
        Response deleteProduct(@Param("id") Long id);
    }


    private Neo4jMovieApi movieApi;
    private JpaProductApi productApi;

    @Before
    public void setupMovieApi() {
        this.movieApi = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logLevel(Logger.Level.BASIC)
                .target(Neo4jMovieApi.class, "http://localhost:" + port);
    }

    @Before
    public void setupProductApi() {
        this.productApi = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logLevel(Logger.Level.BASIC)
                .target(JpaProductApi.class, "http://localhost:" + port);
    }


    @Test
    public void test1_ProductApi() {
        List<Product> response = productApi.getAllProducts();
        softly.then(response).hasSize(3);

        Product expected = response.get(1);
        Product actual = productApi.getProductById(expected.getId());
        softly.then(actual).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @Test
    public void test2_ProductApi_Updates() {
        Product newProduct = Product.builder()
                .name("TEST Product")
                .price(BigDecimal.valueOf(22.99))
                .build();
        Product savedProduct = productApi.saveProduct(newProduct);

        softly.then(savedProduct).isEqualToIgnoringNullFields(newProduct);

        // total should go up by one as we added new product
        List<Product> allProducts = productApi.getAllProducts();
        softly.then(allProducts).hasSize(4);

        savedProduct.setName("TEST Product Updated");
        savedProduct.setPrice(BigDecimal.valueOf(17.50));
        savedProduct.setDescription("Discontinued stock");
        savedProduct = productApi.saveProduct(savedProduct);

        // total should remain same as we updated our product
        allProducts = productApi.getAllProducts();
        softly.then(allProducts).hasSize(4);

        // validate all values
        Product actual = productApi.getProductById(savedProduct.getId());
        softly.then(actual).isEqualToComparingFieldByFieldRecursively(savedProduct);

        Response deleteProduct = productApi.deleteProduct(savedProduct.getId());
        softly.then(deleteProduct.status()).isEqualTo(HttpStatus.OK.value());
    }

    @Autowired
    Neo4jService neo4jService;

    @Before
    public void graphCleanup() {
        Session session = neo4jService.getNeo4jSessionFactory().openSession();
        session.query("MATCH (m:Movie) WHERE m.title =~ 'TEST.*' DELETE m", ImmutableMap.of());
    }


    @Test
    public void test1_MovieApi() {
        List<Movie> response = movieApi.getAllMovies();
        softly.then(response).hasSize(38);

        Movie expected = response.get(10);
        Movie actual = movieApi.getMovieById(expected.getId());
        softly.then(actual).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @Test
    public void test2_MovieApi_Updates() {
        Movie newMovie = Movie.builder()
                .title("TEST Movie")
                .released(2017)
                .stars(3)
                .build();
        Movie savedMovie = movieApi.saveMovie(newMovie);

        softly.then(savedMovie).isEqualToIgnoringNullFields(newMovie);

        // total should go up by one as we added new movie
        List<Movie> allMovies = movieApi.getAllMovies();
        softly.then(allMovies).hasSize(39);

        savedMovie.setTitle("TEST Movie Updated");
        savedMovie.setReleased(2018);
        savedMovie.setStars(5);
        savedMovie = movieApi.saveMovie(savedMovie);

        // total should remain same as we updated our movie
        allMovies = movieApi.getAllMovies();
        softly.then(allMovies).hasSize(39);

        // validate all values
        Movie actual = movieApi.getMovieById(savedMovie.getId());
        softly.then(actual).isEqualToComparingFieldByFieldRecursively(savedMovie);

        Response deleteMovie = movieApi.deleteMovie(savedMovie.getId());
        softly.then(deleteMovie.status()).isEqualTo(HttpStatus.OK.value());
    }
}
