package com.ecommerce.product.service;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.annotation.Timed;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FileStorageService fileStorageService;

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("Creating product: {}", productRequest.getName());

        String savedImageUrl = processImage(productRequest.getVendorEmail(),
                productRequest.getCategory().name(),
                productRequest.getName(),
                productRequest.getImageUrl());

        Product product = Product.builder()
                .sku(productRequest.getSku())
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .stockQuantity(productRequest.getStockQuantity())
                .imageUrl(savedImageUrl)
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

    private String processImage(String vendorEmail, String category, String productName, String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("data:image")) {
            return fileStorageService.saveMerchantImage(vendorEmail, category, productName, imageUrl);
        }
        return imageUrl;
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

    @Timed(value = "product.fetch.time", description = "Time taken to fetch product by ID")
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product fetch failed: ID {} not found", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });

        // Track view asynchronously to reduce latency
        CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send("product-view-events",
                        new ProductViewEvent(product.getId(), product.getSku(), product.getName(),
                                System.currentTimeMillis()));
            } catch (Exception e) {
                log.warn("Failed to send product view event for id: {}: {}", id, e.getMessage());
            }
        });

        return mapToProductResponse(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest, String requesterEmail) {
        log.info("Updating product with id: {} by {}", id, requesterEmail);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product update failed: ID {} not found", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });

        // Ownership check
        if (!product.getVendorEmail().equals(requesterEmail)) {
            log.warn("Unauthorized update attempt for product {} by user {}", id, requesterEmail);
            throw new UnauthorizedException("You do not have permission to update this product");
        }

        String savedImageUrl = processImage(product.getVendorEmail(),
                productRequest.getCategory().name(),
                productRequest.getName(),
                productRequest.getImageUrl());

        product.setSku(productRequest.getSku());
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setImageUrl(savedImageUrl);
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
                .orElseThrow(() -> {
                    log.error("Product deletion failed: ID {} not found", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });

        // Ownership check
        if (!requesterEmail.equals(product.getVendorEmail())) {
            log.warn("Unauthorized deletion attempt for product {} by user {}", id, requesterEmail);
            throw new UnauthorizedException("You do not have permission to delete this product");
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

    public void saveCustomerDesign(Long productId, String customerId, String originalBase64, String editedBase64,
            String instructions) {
        log.info("Saving customer design for product {} and customer {}. Instructions: {}", productId, customerId,
                instructions);
        if (originalBase64 != null) {
            fileStorageService.saveCustomerDesign(customerId, productId, "original", originalBase64);
        }
        if (editedBase64 != null) {
            fileStorageService.saveCustomerDesign(customerId, productId, "edited", editedBase64);
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
