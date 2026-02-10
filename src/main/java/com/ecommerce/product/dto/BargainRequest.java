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
public class BargainRequest {
    private Long productId;
    private BigDecimal currentPrice;
    private BigDecimal minPrice;
    private BigDecimal proposedPrice;
    private String message;
    private String sessionId;
    private java.util.List<BargainMessageDTO> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BargainMessageDTO {
        private String sender;
        private String message;
        private BigDecimal proposedPrice;
    }
}
