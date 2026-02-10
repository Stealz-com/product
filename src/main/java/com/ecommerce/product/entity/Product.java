package com.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;
    private java.time.LocalDateTime createdAt;
    private BigDecimal minPrice;
    private String vendorEmail;

    @Enumerated(EnumType.STRING)
    private CategoryType category;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        if (minPrice == null && price != null) {
            minPrice = price.multiply(new BigDecimal("0.8")); // Default 20% discount floor
        }
    }
}
