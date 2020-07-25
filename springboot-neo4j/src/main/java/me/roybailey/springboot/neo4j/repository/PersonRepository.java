package me.roybailey.springboot.neo4j.repository;

import me.roybailey.springboot.neo4j.domain.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface PersonRepository extends Neo4jRepository<Person,Long> {

    /**
     * Simple auto-derived finder.
     * @param name
     * @return Person object
     */
    Person findByName(String name);

}


