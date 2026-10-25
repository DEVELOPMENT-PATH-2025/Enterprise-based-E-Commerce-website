package com.nexusmart.api.repository;

import com.nexusmart.api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
    
    @Query("SELECT COUNT(o) FROM Order o")
    long countAll();
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal calculateTotalGmv();
}
