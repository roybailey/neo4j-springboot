package me.roybailey.springboot.neo4j.domain;

import lombok.*;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@NodeEntity
public class Person {

    @Id
    @GeneratedValue
    Long id;

    @Getter
    String name;

    @Getter
    Integer born;
}