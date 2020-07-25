package me.roybailey.springboot.jpa.controller;

import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.jpa.domain.Product;
import me.roybailey.springboot.jpa.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping(value = "/product")
    public ResponseEntity<?> getProducts() {
        log.info("getProducts()");
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {

        log.info("getProduct({})", id);
        Product product = productRepository.findById(id).orElse(null);
        if(product != null)
            return ResponseEntity.ok(product);

        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/product")
    public ResponseEntity<?> upsertProduct(@RequestBody Product product) {

        Product result = productRepository.save(product);
        return ResponseEntity.ok(result);
    }


    @DeleteMapping(value = "/product/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {

        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
