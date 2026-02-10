package com.ecommerce.product.controller;

import com.ecommerce.product.client.BargainServiceClient;
import com.ecommerce.product.dto.BargainRequest;
import com.ecommerce.product.dto.BargainResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products/bargain")
@RequiredArgsConstructor
@Slf4j
public class BargainAgentController {

    private final ProductRepository productRepository;
    private final BargainServiceClient bargainServiceClient;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public BargainResponse bargain(@RequestBody BargainRequest request) {
        try {
            log.info("Bargain request received: {}", request);

            if (request == null || request.getProductId() == null) {
                return BargainResponse.builder()
                        .accepted(false)
                        .responseMessage("Invalid request. Product ID is required.")
                        .build();
            }

            Product product = productRepository.findById(request.getProductId()).orElse(null);
            if (product == null) {
                return BargainResponse.builder()
                        .accepted(false)
                        .responseMessage("I couldn't find that product in our catalog.")
                        .build();
            }

            // Enrich request with price info and send to dedicated AI service
            BigDecimal minPrice = product.getMinPrice();
            if (minPrice == null)
                minPrice = product.getPrice().multiply(new java.math.BigDecimal("0.85"));

            request.setCurrentPrice(product.getPrice());
            request.setMinPrice(minPrice);

            return bargainServiceClient.bargain(request);

        } catch (Exception e) {
            log.error("Error calling bargaining microservice", e);
            return BargainResponse.builder()
                    .accepted(false)
                    .responseMessage(
                            "My AI brain is in a different service and having trouble connecting. Try again later!")
                    .build();
        }
    }

    @PostMapping("/train")
    @ResponseStatus(HttpStatus.OK)
    public String train() {
        return bargainServiceClient.train();
    }
}
