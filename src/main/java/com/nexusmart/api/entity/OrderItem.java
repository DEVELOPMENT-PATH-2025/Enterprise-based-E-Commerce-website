package com.nexusmart.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "price_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @Column(name = "fulfillment_status")
    private String fulfillmentStatus = "PENDING";

    @Column(name = "vendor_payout_amount", precision = 10, scale = 2)
    private BigDecimal vendorPayoutAmount;

    public OrderItem() {}

    public OrderItem(Order order, UUID productId, Integer quantity, BigDecimal priceSnapshot) {
        this.order = order;
        this.productId = productId;
        this.quantity = quantity;
        this.priceSnapshot = priceSnapshot;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceSnapshot() {
        return priceSnapshot;
    }

    public void setPriceSnapshot(BigDecimal priceSnapshot) {
        this.priceSnapshot = priceSnapshot;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(String fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public BigDecimal getVendorPayoutAmount() {
        return vendorPayoutAmount;
    }

    public void setVendorPayoutAmount(BigDecimal vendorPayoutAmount) {
        this.vendorPayoutAmount = vendorPayoutAmount;
    }
}
