package me.roybailey.springboot.jpa.controller;

import feign.*;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.jpa.domain.Product;
import org.assertj.core.api.JUnitBDDSoftAssertions;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductControllerTest {

    @LocalServerPort
    int port;

    @Rule
    public TestName name = new TestName();

    @Rule
    public final JUnitBDDSoftAssertions softly = new JUnitBDDSoftAssertions();

    @Headers("Accept: application/json")
    public interface JpaProductApi {

        @RequestLine("GET /product")
        List<Product> getAllProducts();

        @RequestLine("GET /product/{id}")
        Product getProductById(@Param("id") Long id);

        @Headers("Content-Type: application/json")
        @RequestLine("POST /product")
        Product saveProduct(Product product);

        @RequestLine("DELETE /product/{id}")
        Response deleteProduct(@Param("id") Long id);
    }

    private JpaProductApi api;

    @Before
    public void apiSetup() {
        this.api = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logLevel(Logger.Level.BASIC)
                .target(JpaProductApi.class, "http://localhost:" + port);
    }


    @Test
    public void test1_ProductApi() {
        List<Product> response = api.getAllProducts();
        softly.then(response).hasSize(3);

        Product expected = response.get(1);
        Product actual = api.getProductById(expected.getId());
        softly.then(actual).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @Test
    public void test2_ProductApi_Updates() {
        Product newProduct = Product.builder()
                .name("TEST Product")
                .price(BigDecimal.valueOf(22.99))
                .build();
        Product savedProduct = api.saveProduct(newProduct);

        softly.then(savedProduct).isEqualToIgnoringNullFields(newProduct);

        // total should go up by one as we added new product
        List<Product> allProducts = api.getAllProducts();
        softly.then(allProducts).hasSize(4);

        savedProduct.setName("TEST Product Updated");
        savedProduct.setPrice(BigDecimal.valueOf(17.50));
        savedProduct.setDescription("Discontinued stock");
        savedProduct = api.saveProduct(savedProduct);

        // total should remain same as we updated our product
        allProducts = api.getAllProducts();
        softly.then(allProducts).hasSize(4);

        // validate all values
        Product actual = api.getProductById(savedProduct.getId());
        softly.then(actual).isEqualToComparingFieldByFieldRecursively(savedProduct);

        Response deleteProduct = api.deleteProduct(savedProduct.getId());
        softly.then(deleteProduct.status()).isEqualTo(HttpStatus.OK.value());
    }
}

