package me.roybailey.springboot.neo4j.repository;

import me.roybailey.springboot.neo4j.domain.Person;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface PersonRepository extends GraphRepository<Person> {

    /**
     * Simple auto-derived finder.
     * @param name
     * @return Person object
     */
    Person findByName(String name);

}


