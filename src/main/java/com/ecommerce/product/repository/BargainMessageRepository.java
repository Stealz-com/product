package com.ecommerce.product.repository;

import com.ecommerce.product.entity.BargainMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BargainMessageRepository extends JpaRepository<BargainMessage, Long> {
    List<BargainMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);
}
