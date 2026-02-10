package com.ecommerce.product.dto;

import com.ecommerce.product.entity.CategoryType;
import com.ecommerce.product.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer stock; // For frontend compatibility
    private CategoryType category;
    private Gender gender;
    private String imageUrl;
    private java.time.LocalDateTime createdAt;
    private BigDecimal minPrice;
    private String vendorEmail;
}
