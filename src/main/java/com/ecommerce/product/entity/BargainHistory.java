package com.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bargain_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BargainHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String userMessage;
    private String agentMessage;
    private BigDecimal proposedPrice;
    private BigDecimal productPrice;
    private BigDecimal productMinPrice;
    private boolean accepted;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
