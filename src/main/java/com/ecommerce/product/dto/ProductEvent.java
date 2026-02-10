package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEvent {
    private Long productId;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String categoryName;
    private String gender;
    private Integer stockQuantity;
    private String imageUrl;
    private String eventType; // CREATE, UPDATE, DELETE
    private long timestamp;
}
