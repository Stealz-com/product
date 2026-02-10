package com.ecommerce.product.repository;

import com.ecommerce.product.entity.BargainHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BargainHistoryRepository extends JpaRepository<BargainHistory, Long> {
    List<BargainHistory> findByProductId(Long productId);
}
