package com.ecommerce.product.controller;

import com.ecommerce.product.client.BargainServiceClient;
import com.ecommerce.product.dto.BargainRequest;
import com.ecommerce.product.dto.BargainResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/bargain")
@RequiredArgsConstructor
@Slf4j
public class BargainAgentController {

    private final BargainServiceClient bargainServiceClient;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public BargainResponse bargain(@RequestBody BargainRequest request) {
        log.info("Bargain request received but bargaining service is decommissioned: {}", request);
        return BargainResponse.builder()
                .accepted(false)
                .responseMessage(
                        "The bargaining service is currently unavailable. We are working on a better AI for you!")
                .build();
    }

    @PostMapping("/train")
    @ResponseStatus(HttpStatus.OK)
    public String train() {
        return bargainServiceClient.train();
    }
}
