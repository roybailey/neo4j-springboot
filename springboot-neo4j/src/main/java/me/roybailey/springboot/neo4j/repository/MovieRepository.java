package me.roybailey.springboot.neo4j.repository;

import me.roybailey.springboot.neo4j.domain.Movie;
import me.roybailey.springboot.neo4j.domain.Person;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;


public interface MovieRepository extends Neo4jRepository<Movie,Long> {

    /**
     * Simple auto-derived finder by title
     * @param title to match
     * @return movie if found
     */
    Movie findByTitle(String title);

    /**
     * Shows how to provide custom query with parameters.
     * @param yearFrom
     * @param yearUpto
     * @return list of Movie objects
     */
    @Query("MATCH (m:Movie) WHERE m.released >= {0} AND m.released <= {1} RETURN m")
    List<Movie> findMoviesBetween(int yearFrom, int yearUpto);

    /**
     * Shows how to provide custom query with parameters.
     * @param yearFrom
     * @param yearUpto
     * @return list of Movie titles only
     */
    @Query("MATCH (m:Movie) WHERE m.released >= {0} AND m.released <= {1} RETURN m.title")
    List<String> findMovieTitlesBetween(int yearFrom, int yearUpto);

    /**
     * Shows how to provide custom query with parameters.
     * @param name
     * @return Set of Movie objects
     */
    @Query("MATCH (m:Movie)-[ACTED_IN]-(p:Person) WHERE p.name = {0} RETURN m")
    Set<Movie> findByActorName(String name);

    Set<Person> findActedInByTitle(String title);

    @Query("MATCH (p:Person)-[ACTED_IN]->(m:Movie)<-[:DIRECTED]-(d:Person) WHERE p = d RETURN p")
    Set<Person> findActorDirectors();

    @QueryResult
    class MovieData {
        Movie movie;
        Double averageRating;
        Set cast;
    }

    @Query("MATCH (movie:Movie)-[r:RATING]->(), (movie)<-[:ACTS_IN]-(actor:Actor) " +
            "WHERE movie.id={movieId} " +
            "RETURN movie as movie, COLLECT(actor) AS cast, AVG(r.stars) AS averageRating")
    MovieData getMovieData(@Param("movieId") String movieId);


    /**
     * Helper method to wrap a string into Cypher reg-exp match anywhere
     */
    default String cypherLike(String filter) {
        return "(?i).*"+filter+".*";
    }

    /**
     * Find total count of movies matching search string.
     * @param filter
     * @return list of matching records.
     */
    @Query("match (m:Movie) where m.title =~ {filter} return count(m) as total")
    int findMoviesTotal(@Param("filter") String filter);

    /**
     * Find movies matching search string.
     * @param filter
     * @param skip
     * @param limit
     * @return list of matching records
     */
    @Query("match (m:Movie) where m.title =~ {filter} with m order by m.title desc skip {skip} limit {limit} match (m)-[r]-(p:Person) return m,r,p")
    List<Movie> findMovies(
            @Param("filter") String filter,
            @Param("skip") int skip,
            @Param("limit") int limit
    );
}


