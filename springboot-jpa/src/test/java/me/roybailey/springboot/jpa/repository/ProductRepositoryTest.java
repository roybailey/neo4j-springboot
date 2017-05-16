package me.roybailey.springboot.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.jpa.domain.Product;
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

import java.math.BigDecimal;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Rule
    public TestName name= new TestName();

    @Rule
    public final JUnitBDDSoftAssertions softly = new JUnitBDDSoftAssertions();

    @Test
    public void testJpaProductRepository() {
        //setup product
        Product product = Product.builder()
                .name("Matrix DVD Boxset")
                .description("All three Matrix movies")
                .price(BigDecimal.valueOf(14.95))
                .build();

        //save product, verify has ID value after save
        softly.then(product.getId()).isNull(); //null before save
        productRepository.save(product);
        softly.then(product.getId()).isNotNull(); //not null after save
        //fetch from DB
        Product fetchedProduct = productRepository.findOne(product.getId());

        //should not be null
        softly.then(fetchedProduct).isNotNull();

        //should equal
        softly.then(fetchedProduct.getId()).isEqualTo(product.getId());
        softly.then(fetchedProduct.getName()).isEqualTo(product.getName());
        softly.then(fetchedProduct.getPrice()).isEqualTo(product.getPrice());

        //update description and save
        fetchedProduct.setDescription("New Description");
        productRepository.save(fetchedProduct);

        //get from DB, should be updated
        Product fetchedUpdatedProduct = productRepository.findOne(fetchedProduct.getId());
        softly.then(fetchedUpdatedProduct.getDescription()).isEqualTo(fetchedProduct.getDescription());

        //verify count of products in DB
        softly.then(productRepository.count()).isEqualTo(4);
    }
}
