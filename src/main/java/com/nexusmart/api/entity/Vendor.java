package com.nexusmart.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vendors")
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "store_name", nullable = false, unique = true)
    private String storeName;

    @Column(name = "store_slug", nullable = false, unique = true)
    private String storeSlug;

    @Column(name = "business_email", nullable = false)
    private String businessEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorStatus status = VendorStatus.PENDING_APPROVAL;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate = BigDecimal.valueOf(10.00);

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Vendor() {
        this.createdAt = LocalDateTime.now();
    }

    public Vendor(User user, String storeName, String storeSlug, String businessEmail) {
        this.user = user;
        this.storeName = storeName;
        this.storeSlug = storeSlug;
        this.businessEmail = businessEmail;
        this.status = VendorStatus.PENDING_APPROVAL;
        this.commissionRate = BigDecimal.valueOf(10.00);
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreSlug() {
        return storeSlug;
    }

    public void setStoreSlug(String storeSlug) {
        this.storeSlug = storeSlug;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public VendorStatus getStatus() {
        return status;
    }

    public void setStatus(VendorStatus status) {
        this.status = status;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
