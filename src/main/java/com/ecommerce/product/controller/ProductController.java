package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.CustomizationRequest;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        validateMerchantRole(role);
        productRequest.setVendorEmail(email); // Set the creator's email
        return productService.createProduct(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/vendor")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getProductsByVendor(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        validateMerchantRole(role);
        return productService.getProductsByVendor(email);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse updateProduct(@PathVariable Long id, @RequestBody ProductRequest productRequest,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        validateMerchantRole(role);
        return productService.updateProduct(id, productRequest, email);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        validateMerchantRole(role);
        productService.deleteProduct(id, email);
    }

    @PostMapping("/{id}/customization")
    @ResponseStatus(HttpStatus.OK)
    public void saveCustomization(@PathVariable Long id,
            @RequestBody CustomizationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        productService.saveCustomerDesign(id, userId, request.getOriginalImage(), request.getEditedImage(),
                request.getDesignInstructions());
    }

    private void validateMerchantRole(String role) {
        if (!"MERCHANT".equals(role)) {
            throw new RuntimeException("Only merchants can perform this operation");
        }
    }
}
