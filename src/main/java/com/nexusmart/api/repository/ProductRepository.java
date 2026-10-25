package com.nexusmart.api.repository;

import com.nexusmart.api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(value = "SELECT * FROM products ORDER BY vector_embedding <=> CAST(:embedding AS vector) LIMIT :limit", nativeQuery = true)
    List<Product> findNearestNeighbors(@Param("embedding") String embedding, @Param("limit") int limit);

    List<Product> findByCategory(String category);
    List<Product> findByStockLessThan(Integer stock);

    List<Product> findByVendorId(UUID vendorId);
    Page<Product> findByVendorId(UUID vendorId, Pageable pageable);
    long countByVendorId(UUID vendorId);
    long countByVendorIdAndStockLessThan(UUID vendorId, int threshold);
}
