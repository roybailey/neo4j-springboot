package me.roybailey.springboot.jpa.bootstrap;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import me.roybailey.springboot.jpa.domain.Product;
import me.roybailey.springboot.jpa.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ProductLoader implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${spring.jpa.database-platform}")
    String databasePlatform;

    @Autowired
    private ProductRepository productRepository;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        synchronized(databasePlatform) {
            // if we're running H2 (i.e. tests or demo mode) then preload data if not already done so
            if("H2".equals(databasePlatform) && productRepository.count() == 0) {
                preloadProducts();
            }
        }
    }

    private List<Product> preloadProducts() {
        List<Product> products = Collections.emptyList();
        if("H2".equals(databasePlatform)) {
            products = ImmutableList.of(
                    Product.builder()
                            .name("The Matrix mug")
                            .description("Awesome mug with picture of Neo on it")
                            .price(BigDecimal.valueOf(12.99))
                            .build(),
                    Product.builder()
                            .name("The Matrix DVD")
                            .description("Full length feature film")
                            .price(BigDecimal.valueOf(5.99))
                            .build(),
                    Product.builder()
                            .name("The Matrix t-shirt")
                            .description("Awesome t-shirt with picture of Neo on it")
                            .price(BigDecimal.valueOf(34.95))
                            .build()
            );
            System.out.println();
            System.out.println();
            System.out.println();
            productRepository.save(products);
            log.info("Loaded sample products: " + products);
            System.out.println();
            System.out.println();
            System.out.println();
        }
        return products;
    }

}
