package me.roybailey.springboot.neo4j.controller;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.neo4j.domain.Movie;
import me.roybailey.springboot.neo4j.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Slf4j
@Controller
public class MovieController {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieController(MovieRepository repo) {
        this.movieRepository = repo;
    }


    @ResponseBody
    @GetMapping(path = "/movie")
    public ResponseEntity<?> getAllMovies() {
        ResponseEntity<?> response;
        try {
            Iterable<Movie> movies = movieRepository.findAll();
            List<Movie> movieList = StreamSupport.stream(movies.spliterator(), false)
                    .collect(Collectors.toList());
            response = ResponseEntity.ok(movieList);
        } catch (Exception err) {
            response = ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Throwables.getStackTraceAsString(err));
        }
        return response;
    }


    @ResponseBody
    @GetMapping(path = "/movie/{id}")
    public ResponseEntity<?> getMovie(@PathVariable Long id) {
        Movie movie = movieRepository.findById(id).orElse(null);
        return ResponseEntity.ok(movie);
    }


    @ResponseBody
    @PostMapping(path = "/movie")
    public ResponseEntity<?> upsertMovie(@RequestBody(required = true) Movie movie) {
        movieRepository.save(movie);
        return ResponseEntity.ok(movie);
    }


    @ResponseBody
    @DeleteMapping(path = "/movie/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        movieRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
