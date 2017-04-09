package me.roybailey.springboot.jpa.repository;

import me.roybailey.springboot.jpa.domain.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {
}
