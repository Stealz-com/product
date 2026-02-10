package com.ecommerce.product.repository;

import com.ecommerce.product.entity.BargainSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BargainSessionRepository extends JpaRepository<BargainSession, Long> {
    Optional<BargainSession> findByProductIdAndUserIdAndActiveTrue(Long productId, String userId);
}
