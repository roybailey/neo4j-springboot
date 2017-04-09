package me.roybailey.springboot.neo4j.bootstrap;

import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.neo4j.repository.MovieRepository;
import me.roybailey.springboot.neo4j.service.Neo4jService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class MovieLoader implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private Neo4jService neo4jService;

    @Autowired
    private MovieRepository movieRepository;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        synchronized(neo4jService) {
            // if we're running embedded (i.e. tests or demo mode) then preload data if not already done so
            if(neo4jService.isEmbedded() && movieRepository.count() == 0) {
                preloadMovies();
            }
        }
    }

    public void preloadMovies() {
        String cypherCleanup = neo4jService.loadCypher("cypher/delete-movies.cypher");
        neo4jService.runCypher(cypherCleanup);
        String cypherStartup = neo4jService.loadCypher("cypher/create-movies.cypher");
        neo4jService.runCypher(cypherStartup);
        log.info("Loaded Movie database sample data");
    }
}
