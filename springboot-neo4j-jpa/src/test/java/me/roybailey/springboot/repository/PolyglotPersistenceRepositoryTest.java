package me.roybailey.springboot.repository;


import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.jpa.domain.Product;
import me.roybailey.springboot.jpa.repository.ProductRepository;
import me.roybailey.springboot.neo4j.domain.Movie;
import me.roybailey.springboot.neo4j.domain.Person;
import me.roybailey.springboot.neo4j.repository.MovieRepository;
import me.roybailey.springboot.neo4j.repository.PersonRepository;
import me.roybailey.springboot.neo4j.service.Neo4jService;
import me.roybailey.springboot.neo4j.service.PolyglotPersistenceService;
import org.assertj.core.api.JUnitBDDSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PolyglotPersistenceRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    Neo4jService neo4jService;

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    PolyglotPersistenceService polyglotPersistenceService;

    @Rule
    public TestName name= new TestName();

    @Rule
    public final JUnitBDDSoftAssertions softly = new JUnitBDDSoftAssertions();


    private static final int EXPECTED_MOVIE_COUNT = 38;
    private static final int EXPECTED_PERSON_COUNT = 133;
    private static final int EXPECTED_PRODUCT_COUNT = 3;


    @Test
    public void testNeo4jMovieRepository() {

        List<Movie> allMovies = StreamSupport.stream(movieRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        log.info("Loaded movies = " + allMovies);
        assertThat(allMovies).isNotNull().hasSize(EXPECTED_MOVIE_COUNT);

        List<Person> allPeople = StreamSupport.stream(personRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        log.info("Loaded People = " + allPeople);
        assertThat(allPeople).isNotNull().hasSize(EXPECTED_PERSON_COUNT);

        //setup movie
        Movie movie = Movie.builder()
                .title("Arrival")
                .released(2016)
                .actors(ImmutableSet.of(
                        Person.builder().name("Amy Adams").born(1974).build(),
                        Person.builder().name("Jeremy Renner").born(1971).build()))
                .build();

        //save movie, verify has ID value after save
        softly.then(movie.getId()).isNull(); //null before save
        movieRepository.save(movie);
        assertThat(movie.getId()).isNotNull(); //not null after save
        assertThat(personRepository.count()).isEqualTo(EXPECTED_PERSON_COUNT +2);

        //fetch from DB
        Movie fetchedMovie = movieRepository.findOne(movie.getId());

        //should not be null
        assertThat(fetchedMovie).isNotNull();

        //should equal
        softly.then(fetchedMovie.getId()).isEqualTo(movie.getId());
        softly.then(fetchedMovie.getTitle()).isEqualTo(movie.getTitle());
        softly.then(fetchedMovie.getReleased()).isEqualTo(movie.getReleased());

        //update description and save
        fetchedMovie.setTitle("New Title");
        movieRepository.save(fetchedMovie);

        //get from DB, should be updated
        Movie fetchedUpdatedMovie = movieRepository.findOne(fetchedMovie.getId());
        softly.then(fetchedUpdatedMovie.getTitle()).isEqualTo(fetchedMovie.getTitle());

        //verify counts in DB, delete our test data, verify restored counts
        softly.then(movieRepository.count()).isEqualTo(EXPECTED_MOVIE_COUNT +1);
        movieRepository.delete(fetchedUpdatedMovie.getId());
        softly.then(movieRepository.count()).isEqualTo(EXPECTED_MOVIE_COUNT);
        personRepository.delete(movie.getActors());
        softly.then(personRepository.count()).isEqualTo(EXPECTED_PERSON_COUNT);
    }


    @Test
    public void testJpaProductRepository() {
        List<Product> allProducts = StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        log.info("Preloaded Products = " + allProducts);
        assertThat(allProducts).isNotNull().hasSize(EXPECTED_PRODUCT_COUNT);

        //setup product
        Product product = Product.builder()
                .name("Matrix DVD Boxset")
                .description("All three Matrix movies")
                .price(BigDecimal.valueOf(14.95))
                .build();

        //save product, verify has ID value after save
        softly.then(product.getId()).isNull(); //null before save
        productRepository.save(product);
        softly.then(product.getId()).isNotNull(); //not null after save
        //fetch from DB
        Product fetchedProduct = productRepository.findOne(product.getId());

        //should not be null
        softly.then(fetchedProduct).isNotNull();

        //should equal
        softly.then(fetchedProduct.getId()).isEqualTo(product.getId());
        softly.then(fetchedProduct.getName()).isEqualTo(product.getName());
        softly.then(fetchedProduct.getPrice()).isEqualTo(product.getPrice());

        //update description and save
        fetchedProduct.setDescription("New Description");
        productRepository.save(fetchedProduct);

        //get from DB, should be updated
        Product fetchedUpdatedProduct = productRepository.findOne(fetchedProduct.getId());
        softly.then(fetchedUpdatedProduct.getDescription()).isEqualTo(fetchedProduct.getDescription());

        //verify count of products in DB
        softly.then(productRepository.count()).isEqualTo(EXPECTED_PRODUCT_COUNT+1);
        productRepository.delete(fetchedUpdatedProduct.getId());
        softly.then(productRepository.count()).isEqualTo(EXPECTED_PRODUCT_COUNT);

    }

    @Test
    public void testPolyglotPersistenceTransaction() {

        assertThat(productRepository.count()).isEqualTo(EXPECTED_PRODUCT_COUNT);
        assertThat(personRepository.count()).isEqualTo(EXPECTED_PERSON_COUNT);

        Product product = Product.builder()
                .name("TEST Movie")
                .description("test")
                .price(BigDecimal.TEN)
                .build();

        Person person = Person.builder()
                .name("TEST Person")
                .born(1970)
                .build();

        try {
            polyglotPersistenceService.saveAndRollbackTwoDatabaseWrites(product, person);
        } catch (Exception expectedError) {
            log.info("Rolled back transaction involving both JPA and Neo4j");
        }

        //verify count of products and person stores has not changed in DB
        // i.e. that the joint transaction rolled back and didn't commit in either repository
        softly.then(productRepository.count()).isEqualTo(EXPECTED_PRODUCT_COUNT);
        softly.then(personRepository.count()).isEqualTo(EXPECTED_PERSON_COUNT);
    }
}

