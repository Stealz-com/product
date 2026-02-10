package com.ecommerce.product.config;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.entity.CategoryType;
import com.ecommerce.product.entity.Gender;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductService productService;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");
        seedKidsProducts();
    }

    private void seedKidsProducts() {
        log.info("Seeding children's clothing...");

        List<ProductRequest> kidsProducts = Arrays.asList(
                ProductRequest.builder()
                        .sku("KIDS-001")
                        .name("Cotton Colorful T-Shirt")
                        .description("Soft 100% cotton t-shirt with vibrant colors for kids.")
                        .price(new BigDecimal("499.00"))
                        .stockQuantity(100)
                        .category(CategoryType.T_SHIRT)
                        .gender(Gender.KIDS)
                        .imageUrl("https://images.unsplash.com/photo-1519233973511-46a2389e3242?w=500")
                        .minPrice(new BigDecimal("399.00"))
                        .build(),
                ProductRequest.builder()
                        .sku("KIDS-002")
                        .name("Denim Overalls")
                        .description("Durable and stylish denim overalls for active play.")
                        .price(new BigDecimal("1299.00"))
                        .stockQuantity(50)
                        .category(CategoryType.TROUSERS)
                        .gender(Gender.KIDS)
                        .imageUrl("https://images.unsplash.com/photo-1514099119901-b372c51fa497?w=500")
                        .minPrice(new BigDecimal("999.00"))
                        .build(),
                ProductRequest.builder()
                        .sku("KIDS-003")
                        .name("Super Hero Hoodie")
                        .description("Warm and cozy hooded sweatshirt with cool prints.")
                        .price(new BigDecimal("899.00"))
                        .stockQuantity(75)
                        .category(CategoryType.HOODIE)
                        .gender(Gender.KIDS)
                        .imageUrl("https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500")
                        .minPrice(new BigDecimal("699.00"))
                        .build(),
                ProductRequest.builder()
                        .sku("KIDS-004")
                        .name("Floral Summer Dress")
                        .description("Lightweight floral dress perfect for summer outings.")
                        .price(new BigDecimal("799.00"))
                        .stockQuantity(60)
                        .category(CategoryType.DRESS)
                        .gender(Gender.KIDS)
                        .imageUrl("https://images.unsplash.com/photo-1518831959646-742c3a14ebf7?w=500")
                        .minPrice(new BigDecimal("599.00"))
                        .build(),
                ProductRequest.builder()
                        .sku("KIDS-005")
                        .name("Casual Canvas Sneakers")
                        .description("Comfortable everyday sneakers for children.")
                        .price(new BigDecimal("1199.00"))
                        .stockQuantity(40)
                        .category(CategoryType.FOOTWEAR)
                        .gender(Gender.KIDS)
                        .imageUrl("https://images.unsplash.com/photo-1514989940723-e8e51635b782?w=500")
                        .minPrice(new BigDecimal("899.00"))
                        .build());

        kidsProducts.forEach(request -> {
            if (productRepository.findBySku(request.getSku()).isEmpty()) {
                productService.createProduct(request);
                log.info("Seeded kids product: {}", request.getName());
            }
        });
    }
}
