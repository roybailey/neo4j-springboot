package me.roybailey.springboot.neo4j.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@NodeEntity
public class Movie {

    @Id
    @GeneratedValue
    Long id;

    String title;

    Integer stars;

    Integer released;

    @Relationship(type = "ACTED_IN", direction = Relationship.INCOMING)
    Set<Person> actors;

    @Relationship(type = "DIRECTED", direction = Relationship.INCOMING)
    Set<Person> directors;
}
