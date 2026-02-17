package com.ecommerce.product.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final String UPLOAD_DIR = "uploads";

    public FileStorageService() {
        createDirectory(Paths.get(UPLOAD_DIR));
        createDirectory(Paths.get(UPLOAD_DIR, "merchants"));
        createDirectory(Paths.get(UPLOAD_DIR, "customers"));
    }

    public String saveMerchantImage(String vendorEmail, String category, String productName, String base64Image) {
        try {
            String cleanEmail = sanitize(vendorEmail);
            String cleanCategory = sanitize(category);
            String cleanProductName = sanitize(productName);

            Path targetDir = Paths.get(UPLOAD_DIR, "merchants", cleanEmail, cleanCategory);
            createDirectory(targetDir);

            byte[] decodedBytes = decodeBase64(base64Image);
            String fileName = cleanProductName + "_" + UUID.randomUUID().toString().substring(0, 8) + ".png";
            Path filePath = targetDir.resolve(fileName);

            Files.write(filePath, decodedBytes);
            String webPath = "/api/products/uploads/merchants/" + cleanEmail + "/" + cleanCategory + "/" + fileName;
            log.info("Saved merchant image to: {}", filePath);
            return webPath;
        } catch (Exception e) {
            log.error("Failed to save merchant image", e);
            throw new RuntimeException("Could not store merchant image", e);
        }
    }

    public String saveCustomerDesign(String customerId, Long productId, String type, String base64Image) {
        try {
            String cleanCustomerId = sanitize(customerId);
            Path targetDir = Paths.get(UPLOAD_DIR, "customers", cleanCustomerId, String.valueOf(productId));
            createDirectory(targetDir);

            byte[] decodedBytes = decodeBase64(base64Image);
            String fileName = type + "_" + System.currentTimeMillis() + ".png";
            Path filePath = targetDir.resolve(fileName);

            Files.write(filePath, decodedBytes);
            String webPath = "/api/products/uploads/customers/" + cleanCustomerId + "/" + productId + "/" + fileName;
            log.info("Saved customer design ({}) to: {}", type, filePath);
            return webPath;
        } catch (Exception e) {
            log.error("Failed to save customer design", e);
            throw new RuntimeException("Could not store customer design", e);
        }
    }

    private void createDirectory(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (Exception e) {
            log.error("Could not create directory: {}", path, e);
        }
    }

    private byte[] decodeBase64(String base64Image) {
        if (base64Image.contains(",")) {
            base64Image = base64Image.split(",")[1];
        }
        return Base64.getDecoder().decode(base64Image);
    }

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }
}
