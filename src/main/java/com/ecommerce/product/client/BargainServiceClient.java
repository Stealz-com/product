package com.ecommerce.product.client;

import com.ecommerce.product.dto.BargainRequest;
import com.ecommerce.product.dto.BargainResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "bargaining-service", url = "${BARGAINING_SERVICE_URL:http://localhost:8091}")
public interface BargainServiceClient {

    @PostMapping("/api/bargain")
    BargainResponse bargain(@RequestBody BargainRequest request);

    @PostMapping("/api/bargain/train")
    String train();
}
