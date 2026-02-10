package com.ecommerce.product.service;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("Creating product: {}", productRequest.getName());

        Product product = Product.builder()
                .sku(productRequest.getSku())
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .stockQuantity(productRequest.getStockQuantity())
                .imageUrl(productRequest.getImageUrl())
                .minPrice(productRequest.getMinPrice())
                .vendorEmail(productRequest.getVendorEmail())
                .category(productRequest.getCategory())
                .gender(productRequest.getGender())
                .build();

        log.info("Saving product to database: {}", product.getName());
        Product savedProduct = productRepository.save(product);

        // Emit Event
        sendProductEvent(savedProduct, "CREATE");

        return mapToProductResponse(savedProduct);
    }

    @Cacheable(value = "products")
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll()
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByVendor(String vendorEmail) {
        log.info("Fetching products for vendor: {}", vendorEmail);
        return productRepository.findByVendorEmail(vendorEmail)
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Track view
        try {
            kafkaTemplate.send("product-view-events",
                    new ProductViewEvent(product.getId(), product.getSku(), product.getName(),
                            System.currentTimeMillis()));
        } catch (Exception e) {
            log.warn("Failed to send product view event for id: {}", id, e);
        }

        return mapToProductResponse(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest, String requesterEmail) {
        log.info("Updating product with id: {} by {}", id, requesterEmail);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Ownership check
        if (!product.getVendorEmail().equals(requesterEmail)) {
            throw new RuntimeException("You do not have permission to update this product");
        }

        product.setSku(productRequest.getSku());
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setImageUrl(productRequest.getImageUrl());
        product.setMinPrice(productRequest.getMinPrice());
        product.setCategory(productRequest.getCategory());
        product.setGender(productRequest.getGender());

        log.info("Updating product in database: {}", id);
        Product updatedProduct = productRepository.save(product);

        // Emit Event
        sendProductEvent(updatedProduct, "UPDATE");

        return mapToProductResponse(updatedProduct);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id, String requesterEmail) {
        log.info("Deleting product with id: {} by {}", id, requesterEmail);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Ownership check
        if (!requesterEmail.equals(product.getVendorEmail())) {
            throw new RuntimeException("You do not have permission to delete this product");
        }

        productRepository.deleteById(id);

        // Emit Event
        sendProductEvent(product, "DELETE");
    }

    private void sendProductEvent(Product product, String eventType) {
        try {
            ProductEvent event = ProductEvent.builder()
                    .productId(product.getId())
                    .sku(product.getSku())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .categoryName(product.getCategory() != null ? product.getCategory().name() : null)
                    .gender(product.getGender() != null ? product.getGender().name() : null)
                    .stockQuantity(product.getStockQuantity())
                    .imageUrl(product.getImageUrl())
                    .eventType(eventType)
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send("product-events", event);
            log.info("Sent {} event for product: {}", eventType, product.getName());
        } catch (Exception e) {
            log.error("Failed to send product event for id: {}", product.getId(), e);
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .stock(product.getStockQuantity()) // For frontend compatibility
                .category(product.getCategory())
                .gender(product.getGender())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .minPrice(product.getMinPrice())
                .vendorEmail(product.getVendorEmail())
                .build();
    }
}
