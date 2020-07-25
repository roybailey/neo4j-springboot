package me.roybailey.springboot.neo4j.repository;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.neo4j.domain.Movie;
import me.roybailey.springboot.neo4j.domain.Person;
import me.roybailey.springboot.neo4j.service.Neo4jService;
import org.assertj.core.api.JUnitBDDSoftAssertions;
import org.assertj.core.api.SoftAssertionListAssert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MovieRepositoryTest {


    @Autowired
    MovieRepository movieRepository;

    @Autowired
    PersonRepository personRepository;

    private static final int EXPECTED_MOVIE_COUNT = 38;
    private static final int EXPECTED_PERSON_COUNT = 133;


    @Rule
    public TestName name= new TestName();

    @Rule
    public final JUnitBDDSoftAssertions softly = new JUnitBDDSoftAssertions();



    @Test
    public void testNeo4jMovieRepositoryQueries() {

        List<Movie> allMovies = StreamSupport.stream(movieRepository.findAll().spliterator(), false).collect(Collectors.toList());
        log.info("Matrix movie = " + allMovies);
        softly.then(allMovies).isNotNull().hasSize(38);

        Movie matrix = movieRepository.findByTitle("The Matrix");
        log.info("Matrix movie = " + matrix);
        softly.then(matrix).isNotNull().hasFieldOrProperty("title");

        List<Movie> ninetiesMovies = movieRepository.findMoviesBetween(1990,1999);
        log.info("Nineties movies = " + ninetiesMovies);
        softly.then(ninetiesMovies).isNotNull().hasSize(20);

        Set<Movie> tomHanksMovies = movieRepository.findByActorName("Tom Hanks");
        log.info("Tom Hanks movies = " + tomHanksMovies);
        softly.then(tomHanksMovies).isNotNull().hasSize(12);

        Set<Person> actorsInTheMatrix = movieRepository.findByTitle("The Matrix").getActors();
        log.info("Actors in The Matrix = " + actorsInTheMatrix);
        softly.then(actorsInTheMatrix).isNotNull().hasSize(5);

        Set<Person> actorDirectors = movieRepository.findActorDirectors();
        log.info("Actor Directors = " + actorDirectors);
        softly.then(actorDirectors).isNotNull().hasSize(8);
    }


    private SoftAssertionListAssert<Movie> assertFindMovies(
            String name,
            String filter,
            int skip,
            int limit,
            int expectedTotal
    ) {
        String cypherFilter = movieRepository.cypherLike(filter);
        int actualTotal = movieRepository.findMoviesTotal(cypherFilter);
        List<Movie> allMovies = movieRepository.findMovies(cypherFilter, skip, limit);
        log.info(name+" = " + allMovies);
        softly.then(actualTotal).isEqualTo(expectedTotal);
        return softly.then(allMovies);
    }

    @Test
    public void testMovieRepository_findMoviesPagination() {

        assertFindMovies("allMovies", "", 0, 10, 38).hasSize(10);
        assertFindMovies("theMoviesPage1", "the", 0, 10, 12).hasSize(10);
        assertFindMovies("theMoviesPage2", "the", 10, 10, 12).hasSize(2);
        assertFindMovies("matrixMovies", "matrix", 0, 10, 3).hasSize(3);

    }

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
        softly.then(movie.getId()).isNotNull(); //not null after save
        softly.then(personRepository.count()).isEqualTo(EXPECTED_PERSON_COUNT +2);

        //fetch from DB
        Movie fetchedMovie = movieRepository.findById(movie.getId()).orElse(null);

        //should not be null
        softly.then(fetchedMovie).isNotNull();

        //should equal
        softly.then(fetchedMovie.getId()).isEqualTo(movie.getId());
        softly.then(fetchedMovie.getTitle()).isEqualTo(movie.getTitle());
        softly.then(fetchedMovie.getReleased()).isEqualTo(movie.getReleased());

        //update description and save
        fetchedMovie.setTitle("New Title");
        movieRepository.save(fetchedMovie);

        //get from DB, should be updated
        Movie fetchedUpdatedMovie = movieRepository.findById(fetchedMovie.getId()).orElse(null);
        softly.then(fetchedUpdatedMovie.getTitle()).isEqualTo(fetchedMovie.getTitle());

        //verify counts in DB, delete our test data, verify restored counts
        softly.then(movieRepository.count()).isEqualTo(EXPECTED_MOVIE_COUNT +1);
        movieRepository.deleteById(fetchedUpdatedMovie.getId());
        softly.then(movieRepository.count()).isEqualTo(EXPECTED_MOVIE_COUNT);
        personRepository.deleteAll(movie.getActors());
        softly.then(personRepository.count()).isEqualTo(EXPECTED_PERSON_COUNT);
    }
}
