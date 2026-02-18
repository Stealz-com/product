package com.ecommerce.product.config;

import com.ecommerce.product.entity.CategoryType;
import com.ecommerce.product.entity.Gender;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            log.info("Seeding initial product data...");

            Product p1 = Product.builder()
                    .sku("TSH-BLK-001")
                    .name("Classic Black T-Shirt")
                    .description("Premium cotton black t-shirt, perfect for everyday wear.")
                    .price(new BigDecimal("899.00"))
                    .minPrice(new BigDecimal("700.00"))
                    .stockQuantity(50)
                    .category(CategoryType.T_SHIRT)
                    .gender(Gender.MEN)
                    .vendorEmail("vendor@example.com")
                    .imageUrl(
                            "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?q=80&w=1000&auto=format&fit=crop")
                    .build();

            Product p2 = Product.builder()
                    .sku("TSH-WHT-002")
                    .name("Minimalist White T-Shirt")
                    .description("Clean and crisp white t-shirt made from sustainable materials.")
                    .price(new BigDecimal("799.00"))
                    .minPrice(new BigDecimal("600.00"))
                    .stockQuantity(30)
                    .category(CategoryType.T_SHIRT)
                    .gender(Gender.WOMEN)
                    .vendorEmail("vendor@example.com")
                    .imageUrl(
                            "https://images.unsplash.com/photo-1554568218-0f1715e72254?q=80&w=1000&auto=format&fit=crop")
                    .build();

            Product p3 = Product.builder()
                    .sku("JNS-BLU-003")
                    .name("Heritage Blue Jeans")
                    .description("Classic straight-cut blue jeans with a vintage wash.")
                    .price(new BigDecimal("2499.00"))
                    .minPrice(new BigDecimal("2000.00"))
                    .stockQuantity(25)
                    .category(CategoryType.JEANS)
                    .gender(Gender.MEN)
                    .vendorEmail("vendor@example.com")
                    .imageUrl(
                            "https://images.unsplash.com/photo-1542272604-787c3835535d?q=80&w=1000&auto=format&fit=crop")
                    .build();

            productRepository.saveAll(Arrays.asList(p1, p2, p3));
            log.info("Successfully seeded {} products.", productRepository.count());
        } else {
            log.info("Database already contains data, skipping seeding.");
        }
    }
}
