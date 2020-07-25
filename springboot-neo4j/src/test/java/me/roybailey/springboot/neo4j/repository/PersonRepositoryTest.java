package me.roybailey.springboot.neo4j.repository;

import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.neo4j.domain.Person;
import org.assertj.core.api.JUnitBDDSoftAssertions;
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

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersonRepositoryTest {


    @Autowired
    PersonRepository personRepository;

    private static final int EXPECTED_PERSON_COUNT = 133;


    @Rule
    public TestName name= new TestName();

    @Rule
    public final JUnitBDDSoftAssertions softly = new JUnitBDDSoftAssertions();

    
    @Test
    public void testNeo4jPersonRepository() {

        assertThat(personRepository.count()).isEqualTo(EXPECTED_PERSON_COUNT);

        //setup person
        Person person = Person.builder().name("Amy Adams").born(1974).build();

        //save person, verify has ID value after save
        softly.then(person.getId()).isNull(); //null before save
        personRepository.save(person);
        softly.then(person.getId()).isNotNull(); //not null after save
        softly.then(personRepository.count()).isEqualTo(EXPECTED_PERSON_COUNT +1);

        //fetch from DB
        Person fetchedPerson = personRepository.findById(person.getId()).orElse(null);

        //should not be null
        softly.then(fetchedPerson).isNotNull();

        //should equal
        softly.then(fetchedPerson.getId()).isEqualTo(person.getId());
        softly.then(fetchedPerson.getName()).isEqualTo(person.getName());
        softly.then(fetchedPerson.getBorn()).isEqualTo(person.getBorn());

        //update description and save
        fetchedPerson.setName("New Name");
        personRepository.save(fetchedPerson);

        //get from DB, should be updated
        Person fetchedUpdatedPerson = personRepository.findById(fetchedPerson.getId()).orElse(null);
        softly.then(fetchedUpdatedPerson.getName()).isEqualTo(fetchedPerson.getName());

        //verify counts in DB, delete our test data, verify restored counts
        softly.then(personRepository.count()).isEqualTo(EXPECTED_PERSON_COUNT+1);
        personRepository.deleteById(person.getId());
        softly.then(personRepository.count()).isEqualTo(EXPECTED_PERSON_COUNT);
    }
}
