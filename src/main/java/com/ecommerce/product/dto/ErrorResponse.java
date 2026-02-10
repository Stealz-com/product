package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private int httpCode;
    private String message;
    private LocalDateTime timestamp;
    private List<String> errors;

    public ErrorResponse(int httpCode, String message, List<String> errors) {
        this.success = false;
        this.httpCode = httpCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
}
