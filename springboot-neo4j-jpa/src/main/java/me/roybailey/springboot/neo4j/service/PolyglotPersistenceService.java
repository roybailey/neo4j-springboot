package me.roybailey.springboot.neo4j.service;


import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.jpa.domain.Product;
import me.roybailey.springboot.jpa.repository.ProductRepository;
import me.roybailey.springboot.neo4j.domain.Person;
import me.roybailey.springboot.neo4j.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PolyglotPersistenceService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PersonRepository personRepository;

    @Transactional
    public void saveAndRollbackTwoDatabaseWrites(Product product, Person person) {
        //save product, verify has ID value after save
        productRepository.save(product);
        log.info("Saved new JPA product");
        //save the person, verify has ID value after save
        personRepository.save(person);
        log.info("Saved new Neo4j person");
        throw new RuntimeException();
    }
}

