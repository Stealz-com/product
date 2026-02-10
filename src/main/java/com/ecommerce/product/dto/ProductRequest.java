package com.ecommerce.product.dto;

import com.ecommerce.product.entity.CategoryType;
import com.ecommerce.product.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private CategoryType category;
    private Gender gender;
    private String imageUrl;
    private BigDecimal minPrice;
    private String vendorEmail;
}
