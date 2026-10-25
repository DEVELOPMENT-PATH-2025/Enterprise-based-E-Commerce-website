package com.nexusmart.api.repository;

import com.nexusmart.api.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    Optional<Vendor> findByUserId(UUID userId);
    Optional<Vendor> findByStoreSlug(String storeSlug);
    List<Vendor> findByStatus(String status);
    long countByStatus(String status);
}
