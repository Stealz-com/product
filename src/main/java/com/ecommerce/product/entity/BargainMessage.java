package com.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bargain_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BargainMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;

    private String sender; // USER, AI

    @Column(columnDefinition = "TEXT")
    private String message;

    private BigDecimal proposedPrice;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
